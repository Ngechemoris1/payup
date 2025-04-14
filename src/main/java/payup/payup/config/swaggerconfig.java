package payup.payup.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    /**
     * Configures Swagger for API documentation.
     * 
     * @return OpenAPI object for Swagger configuration.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PayUp API")
                        .version("1.0")
                        .description("API for managing properties, tenants, and rents")
                        .termsOfService("urn:tos")
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")));
    }
}