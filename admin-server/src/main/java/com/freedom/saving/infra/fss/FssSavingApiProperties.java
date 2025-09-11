package com.freedom.saving.infra.fss;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "fss")
public class FssSavingApiProperties {

    @NotBlank(message = "fss.base-url은 빈 값일 수 없습니다.")
    private String baseUrl;

    @NotBlank(message = "fss.api-key는 빈 값일 수 없습니다.")
    private String apiKey;
}
