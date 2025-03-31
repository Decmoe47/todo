package com.decmoe47.todo.config.property;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@ConfigurationProperties(prefix = "security")
@Validated
public class SecurityProperties {

    @Min(-1)
    private Integer accessTokenTimeToLive = 3600;

    @Min(-1)
    private Integer refreshTokenTimeToLive = 604800;

    @NotNull
    private String secretKey;
}
