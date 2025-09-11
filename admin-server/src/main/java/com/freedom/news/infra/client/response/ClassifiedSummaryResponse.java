package com.freedom.news.infra.client.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ClassifiedSummaryResponse {

    @JsonProperty("is_economic")
    private boolean economic;

    @JsonProperty("summary")
    private String summary;

    @JsonProperty("reason")
    private String reason;
}


