package com.freedom.saving.infra.fss;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.function.Function;

/**
 * 금감원 API 전용 WebClient 빈 구성.
 * - 연결/응답 타임아웃 설정
 * - 에러 상태 코드 로깅 필터 추가(본문 소비 없이 상태/URL만 기록)
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(FssSavingApiProperties.class)
public class FssWebClientConfig {

    @Bean(name = "fssWebClient")
    public WebClient fssWebClient(FssSavingApiProperties props) {
        HttpClient httpClient = HttpClient.create()
                // 연결 타임아웃(밀리초)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                // 응답 전체 타임아웃
                .responseTimeout(Duration.ofSeconds(10))
                // 소켓 Read/Write 타임아웃 핸들러
                .doOnConnected(conn -> {
                    conn.addHandlerLast(new ReadTimeoutHandler(10));
                    conn.addHandlerLast(new WriteTimeoutHandler(10));
                });

        return WebClient.builder()
                .baseUrl(props.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(errorLoggingFilter())
                .build();
    }

    /**
     * 에러 상태일 때만 간단히 로그를 남기는 필터
     */
    private ExchangeFilterFunction errorLoggingFilter() {
        return new ExchangeFilterFunction() {
            @Override
            public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
                return next.exchange(request).map(new Function<ClientResponse, ClientResponse>() {
                    @Override
                    public ClientResponse apply(ClientResponse response) {
                        if (response.statusCode().isError()) {
                            log.warn("[FSS] {} {} -> {}", request.method(), request.url(), response.statusCode());
                        }
                        return response;
                    }
                });
            }
        };
    }
}
