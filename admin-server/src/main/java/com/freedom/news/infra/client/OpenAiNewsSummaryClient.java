package com.freedom.news.infra.client;

import com.freedom.news.infra.client.response.ClassifiedSummaryResponse;
import com.freedom.news.infra.client.response.SummaryResponse;
import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.StructuredChatCompletionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OpenAiNewsSummaryClient {
    private static final int LIMIT = 150;
    private final OpenAIClient openAIClient;

    public String summarize(String plainText) {
        String prompt = """
            당신은 한국어 뉴스 요약 전문가입니다.
            아래 뉴스 본문을 읽고 조건을 반드시 지켜 1문장 요약을 작성하세요.

            [조건]
            - 본문에 없는 내용을 추가하거나 수정하지 마세요.
            - 뉴스의 핵심 포인트를 한눈에 들어오게 작성하세요.
            - 핵심 주체/행위/수치/날짜가 있으면 포함하세요.
            - 기사 본문에 나온 사실만 사용하세요.
            - 본문에 없는 정보·추측·해석·평가·의견·의역·보정 금지합니다.
            - 출력은 오직 JSON: {"summary":"..."} (앞뒤 설명, 개행, 코드블록 없음)

            [뉴스 본문]
            %s
            """.formatted(plainText);

        StructuredChatCompletionCreateParams<SummaryResponse> params =
                StructuredChatCompletionCreateParams.<SummaryResponse>builder()
                        .model(ChatModel.GPT_4_1)
                        .temperature(0.2)
                        .maxCompletionTokens(220)
                        .responseFormat(SummaryResponse.class)
                        .addUserMessage(prompt)
                        .build();

        return openAIClient.chat()
                .completions()
                .create(params)
                .choices()
                .stream()
                .flatMap(choice -> choice.message().content().stream())
                .map(SummaryResponse::getSummary)
                .findFirst()
                .orElse("");
    }

    public ClassifiedSummaryResponse classifyAndSummarize(String plainText) {
        String prompt = """
            너는 한국 '경제 뉴스 분류/요약기'다. 아래 [본문]만 사용하고, 외부 지식·추론·상상·가정은 금지한다.
            
            [판단 대상(본문에 직접 언급이 있을 때만 true)]
            - 청년 정책: 청년 대상 정책/지원/예산/세제/고용 프로그램 (청년 =대상 정책 필수 확인)
            - 금융: 금리/대출/예금/은행/주식·채권/환율/금융감독
            - 거시경제: 물가/성장률/GDP/고용지표/무역/산업생산/기준금리/재정·통화정책
            ※ 위에 직접 해당하지 않으면 false
            
            [요약 규칙 - 중요]
            - is_economic=true일 때만 summary 작성
            - summary는 1~2문장, 총 150자 이내
            - 본문에 없는 사실·숫자·고유명사 생성 금지(추가 정보·배경 지식 금지)
            - 가능하면 본문 표현을 그대로 사용하고 군더더기 문구 제거
            - 150자를 넘기면 불필요한 수식어를 삭제해 150자 이내로 축약
            
            [출력(JSON만)]
            {"is_economic": true|false, "summary": "경제 기사면 1~2문장 150자 이내 요약, 아니면 빈 문자열", "reason": "비경제/비해당 사유(선택)"}
            
            [본문]
            %s
            """.formatted(plainText);

        StructuredChatCompletionCreateParams<ClassifiedSummaryResponse> params =
                StructuredChatCompletionCreateParams.<ClassifiedSummaryResponse>builder()
                        .model(ChatModel.GPT_4_1)
                        .temperature(0.2)
                        .maxCompletionTokens(280)
                        .responseFormat(ClassifiedSummaryResponse.class)
                        .addUserMessage(prompt)
                        .build();

        return openAIClient.chat()
                .completions()
                .create(params)
                .choices()
                .stream()
                .flatMap(choice -> choice.message().content().stream())
                .findFirst()
                .orElseGet(ClassifiedSummaryResponse::new);
    }
}
