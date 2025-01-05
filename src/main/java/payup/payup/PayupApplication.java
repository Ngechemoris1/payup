package payup.payup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@EnableAsync // Enable asynchronous method execution
@EnableScheduling // Enable scheduling for periodic tasks like rent reminders
@EnableJpaAuditing // Enable JPA auditing for tracking changes
@OpenAPIDefinition(info = @Info(title = "PayUp API", version = "1.0", description = "API for managing properties, tenants, and rents"))
public class PayUpApplication {

    /**
     * The main entry point for the application.
     * 
     * @param args Command line arguments for the application.
     */
    public static void main(String[] args) {
        SpringApplication.run(PayUpApplication.class, args);
    }
}