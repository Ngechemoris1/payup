package payup.payup.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for M-Pesa API settings, populated from application properties.
 * Uses Lombok's @Data to generate getters, setters, and toString methods.
 */
@Configuration
@ConfigurationProperties(prefix = "mpesa")
@Data
public class MpesaConfig { // Renamed to follow Java naming conventions
    private String consumerKey;
    private String consumerSecret;
    private String shortcode;
    private String passkey;
    private String callbackUrl;
    private String environment;
}