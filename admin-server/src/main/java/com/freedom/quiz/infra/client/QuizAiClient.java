package com.freedom.quiz.infra.client;

import com.freedom.quiz.infra.client.response.HintResponse;
import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.StructuredChatCompletionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuizAiClient {

    private final OpenAIClient openAIClient;

    public String generateHint(String quizData) {
        try {
            String prompt = """
                너는 '퀴즈 힌트 생성 전문가'다. 아래 퀴즈 정보를 바탕으로 적절한 힌트를 생성해줘.
                
                [힌트 생성 규칙]
                1. 정답을 직접 알려주지 말고, 문제 해결의 방향이나 핵심 키워드만 암시해줘
                2. 객관식 문제의 경우: 정답 선택지를 직접 언급하지 말고, 정답과 관련된 개념이나 분야를 암시해줘
                3. OX 문제의 경우: 참/거짓을 직접 말하지 말고, 판단 기준이 되는 핵심 포인트를 제시해줘
                4. 해설의 핵심 내용 중 1-2개 키워드나 개념을 활용해서 힌트를 만들어줘
                5. 1-2문장, 총 60자 이내로 간결하게 작성해줘
                6. "~에 대해 생각해보세요", "~를 고려해보세요" 등 친근한 어조로 작성해줘
                
                [금지사항]
                - 정답 번호나 정답 내용을 직접 언급하지 말 것
                - "정답은", "답은", "맞는 것은" 등의 표현 사용 금지
                - 해설에 없는 추가 정보나 배경지식 사용 금지
                
                [출력 형식(JSON만)]
                {"hint": "생성된 힌트 문장", "is_valid": true}
                
                [퀴즈 정보]
                %s
                """.formatted(quizData);
            
            StructuredChatCompletionCreateParams<HintResponse> params =
                    StructuredChatCompletionCreateParams.<HintResponse>builder()
                            .model(ChatModel.GPT_4_1)
                            .temperature(0.4)
                            .maxCompletionTokens(150)
                            .responseFormat(HintResponse.class)
                            .addUserMessage(prompt)
                            .build();

            HintResponse response = openAIClient.chat()
                    .completions()
                    .create(params)
                    .choices()
                    .stream()
                    .flatMap(choice -> choice.message().content().stream())
                    .findFirst()
                    .orElseGet(HintResponse::new);

            if (response.getHint() == null || response.getHint().trim().isEmpty()) {
                return "문제의 핵심 키워드를 중심으로 생각해보세요.";
            }

            return response.getHint().trim();
            
        } catch (Exception e) {
            log.error("AI 힌트 생성 실패", e);
            return "문제의 핵심 키워드를 중심으로 생각해보세요.";
        }
    }
}
