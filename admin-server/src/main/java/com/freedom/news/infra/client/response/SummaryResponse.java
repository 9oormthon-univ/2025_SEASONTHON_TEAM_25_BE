package com.freedom.news.infra.client.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SummaryResponse {
    @JsonProperty("summary")
    private String summary;
}
