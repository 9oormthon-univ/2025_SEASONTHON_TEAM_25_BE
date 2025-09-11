package com.freedom.news.infra.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.freedom.news.infra.client.response.NewsItem;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PolicyNewsClient {

    @Value("${news.policy-briefing.service-key}")
    private String serviceKey;

    private final XmlMapper xmlMapper = new XmlMapper();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public List<NewsItem> getNewsAPI() {
        String today = LocalDate.now().format(DATE_FORMATTER);
        return fetchNews(today, today);
    }

    private List<NewsItem> fetchNews(String startDate, String endDate) {
        try {
            String urlString = buildApiUrl(startDate, endDate);
            String xmlResponse = executeHttpRequest(urlString);
            return parseNewsItemsDirectly(xmlResponse);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private String buildApiUrl(String startDate, String endDate) throws Exception {
        return "http://apis.data.go.kr/1371000/policyNewsService/policyNewsList" + "?" + URLEncoder.encode("serviceKey", StandardCharsets.UTF_8) + "=" + serviceKey +
                "&" + URLEncoder.encode("startDate", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(startDate, StandardCharsets.UTF_8) +
                "&" + URLEncoder.encode("endDate", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(endDate, StandardCharsets.UTF_8);
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

    private List<NewsItem> parseNewsItemsDirectly(String xmlResponse) throws Exception {
        try {
            JsonNode rootNode = xmlMapper.readTree(xmlResponse);
            JsonNode itemsNode = rootNode.at("/body/NewsItem");
            List<NewsItem> items = new ArrayList<>();
            if (itemsNode.isArray()) {
                // 복수 아이템인 경우
                for (JsonNode itemNode : itemsNode) {
                    NewsItem item = xmlMapper.treeToValue(itemNode, NewsItem.class);
                    items.add(item);
                }
            } else if (!itemsNode.isMissingNode()) {
                // 단일 아이템인 경우
                NewsItem item = xmlMapper.treeToValue(itemsNode, NewsItem.class);
                items.add(item);
            }
            return items;
        } catch (Exception e) {
            throw new RuntimeException("NewsItem 파싱 실패", e);
        }
    }
}
