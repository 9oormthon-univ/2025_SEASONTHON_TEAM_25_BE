package com.freedom.saving.infra.fss;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Component
public class FssSavingApiClient {

    private final WebClient webClient;
    private final FssSavingApiProperties props;

    public FssSavingApiClient(@Qualifier("fssWebClient") WebClient webClient,
                              FssSavingApiProperties props) {
        this.webClient = webClient;
        this.props = props;
    }

    /**
     * @param topFinGrpNo 020000: 은행
     * @param pageNo    페이지 번호
     */
    public Mono<FssSavingResponseDto> fetchSavings(String topFinGrpNo, int pageNo) {
        String uri = UriComponentsBuilder.fromPath("/finlifeapi/savingProductsSearch.json")
                .queryParam("auth", props.getApiKey())     // 인증키는 프로퍼티에서 주입
                .queryParam("topFinGrpNo", topFinGrpNo)
                .queryParam("pageNo", pageNo)
                .toUriString();

        // 여기서는 상태 코드만 검사(4xx/5xx -> WebClientResponseException)
        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(FssSavingResponseDto.class);
    }
}
