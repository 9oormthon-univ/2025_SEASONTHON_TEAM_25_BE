package com.freedom.quiz.infra.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freedom.quiz.infra.client.response.ExternalQuizApiResponse;
import com.freedom.quiz.infra.client.response.ExternalQuizItem;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ExternalQuizClient {

    @Value("${external.quiz.api.service-key}")
    private String serviceKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ExternalQuizApiResponse getQuizAPI() {
        try {
            String apiUrl = "https://api.odcloud.kr/api/15131287/v1/uddi:28b321b1-0351-4451-956c-8e197151877f";
            String urlString = apiUrl + "?returnType=JSON&" + URLEncoder.encode("serviceKey", StandardCharsets.UTF_8) + "=" + serviceKey + "&perPage=200";
            String jsonResponse = executeHttpRequest(urlString);
            return parseQuizResponse(jsonResponse);
        } catch (Exception e) {
            return ExternalQuizApiResponse.empty();
        }
    }

    private String executeHttpRequest(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        try {
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-type", "application/json");
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);

            String result = getResult(conn);

            if (result.contains("SERVICE_KEY_IS_NOT_REGISTERED_ERROR")) {
                throw new RuntimeException("서비스키 미등록 오류");
            } else if (result.contains("APPLICATION_ERROR")) {
                throw new RuntimeException("API 애플리케이션 오류");
            } else if (result.contains("INVALID_REQUEST_PARAMETER_ERROR")) {
                throw new RuntimeException("잘못된 요청 파라미터");
            }

            return result;
        } finally {
            conn.disconnect();
        }
    }

    private static String getResult(HttpURLConnection conn) throws IOException {
        int responseCode = conn.getResponseCode();

        BufferedReader reader = (responseCode >= 200 && responseCode <= 300)
                ? new BufferedReader(new InputStreamReader(conn.getInputStream()))
                : new BufferedReader(new InputStreamReader(conn.getErrorStream()));

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        return response.toString();
    }

    private ExternalQuizApiResponse parseQuizResponse(String jsonResponse) throws Exception {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            
            JsonNode dataNode = rootNode.path("data");

            List<ExternalQuizItem> quizItems = new ArrayList<>();

            if (dataNode.isArray()) {
                for (JsonNode itemNode : dataNode) {
                    ExternalQuizItem item = objectMapper.treeToValue(itemNode, ExternalQuizItem.class);
                    quizItems.add(item);
                }
            }
            return ExternalQuizApiResponse.builder()
                    .currentCount(rootNode.path("currentCount").asInt(0))
                    .matchCount(rootNode.path("matchCount").asInt(0))
                    .data(quizItems)
                    .page(rootNode.path("page").asInt(1))
                    .perPage(rootNode.path("perPage").asInt(200))
                    .totalCount(rootNode.path("totalCount").asInt(0))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("외부 퀴즈 API 응답 파싱 실패", e);
        }
    }
}
