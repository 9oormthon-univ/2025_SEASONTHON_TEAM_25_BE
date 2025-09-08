package com.freedom.news.infra.client;

import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.StructuredChatCompletionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NewsQuizGenerationClient {

    public record OxQuiz(String question, boolean answer, String explanation) {}
    public record McqOption(String text, boolean correct) {}
    public record McqQuiz(String question, List<McqOption> options, String explanation) {}
    public record QuizPack(OxQuiz ox, McqQuiz mcq, String category) {}

    private final OpenAIClient openAIClient;

    public QuizPack generateQuizzes(String title, String summary, String plainText) {
        String prompt = """
            너는 '뉴스 기반 퀴즈 제작자'다.
            
            [절대 규칙]
            - 아래 제공되는 [제목], [AI 요약], [본문] 텍스트만 사용한다. 외부 지식/추론/상상 금지.
            - 본문에 경제, 청년정책, 금융 관련 내용을 바탕으로 문제 생성
            - 질문·정답·해설의 모든 사실은 [본문]에 ‘직접 등장하는 문장’으로 검증 가능해야 한다.
            - 해설에는 [본문]에서 발췌한 5~20자 내 짧은 인용을 큰따옴표로 반드시 1회 포함한다.
            - 고유명사/수치/날짜는 [본문] 표기를 그대로 사용한다. 새로운 값 창작 금지.
            - 출력은 **JSON만** 허용한다. 어떤 설명/주석/코드펜스/텍스트도 JSON 밖에 출력하지 않는다.
            - JSON은 유효해야 한다: 모든 문자열은 " 로 감싸고, 줄바꿈은 \\n 으로 이스케이프하며, 마지막 원소 뒤 쉼표 금지.
            
            [출력 스키마(정확히 준수)]
            {
              "ox": {"question":"...", "answer":true|false, "explanation":"..."},
              "mcq": {
                "question":"...",
                "options":[
                  {"text":"...", "correct":true|false},
                  {"text":"...", "correct":true|false},
                  {"text":"...", "correct":true|false},
                  {"text":"...", "correct":true|false}
                ],
                "explanation":"..."
              },
              "category":"뉴스 기사"
            }
            
            [문항 작성 규칙]
            - OX: 본문 한 문장만으로 참/거짓이 단정 가능한 문장 1개(60자 이내).
            - 4지선다: 보기 4개, 오직 1개만 correct=true. 나머지 3개는 본문과 모순되거나 본문에 없는 값.
            - mcq.question 60자 이내, 각 options[*].text 5~40자.
            - explanation은 각 1~2문장 + 본문 인용 1개(예: "…원문 일부…").
            
            [입력]
            [제목] %s
            [AI 요약] %s
            [본문] %s
            """.formatted(title, (summary == null ? "" : summary), plainText);

        StructuredChatCompletionCreateParams<QuizPack> params =
                StructuredChatCompletionCreateParams.<QuizPack>builder()
                        .model(ChatModel.GPT_4_1)
                        .temperature(0.2)
                        .maxCompletionTokens(600)
                        .responseFormat(QuizPack.class)
                        .addUserMessage(prompt)
                        .build();

        return openAIClient.chat()
                .completions()
                .create(params)
                .choices()
                .stream()
                .flatMap(c -> c.message().content().stream())
                .findFirst()
                .orElse(null);
    }
}


