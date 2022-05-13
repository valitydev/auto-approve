package dev.vality.auto.approve.configuration.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "claim.management.client")
public class ClaimManagementClientProperties {

    @NotNull
    private Resource url;

    @NotNull
    private int networkTimeout = 5000;
}
