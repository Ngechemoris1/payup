package payup.payup.config;

import org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.endpoint.web.*;
import org.springframework.boot.actuate.endpoint.ExposableEndpoint;
import org.springframework.boot.actuate.endpoint.web.servlet.WebMvcEndpointHandlerMapping;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Configuration
public class ActuatorConfig {

    /**
     * Configures the WebMvcEndpointHandlerMapping for actuator endpoints.
     *
     * @return WebMvcEndpointHandlerMapping for handling actuator endpoints.
     */
    @Bean
    public WebMvcEndpointHandlerMapping webEndpointServletHandlerMapping(
            WebEndpointsSupplier webEndpointsSupplier,
            EndpointMediaTypes endpointMediaTypes,
            CorsEndpointProperties corsProperties,
            WebEndpointProperties webEndpointProperties,
            Environment environment) {
        List<ExposableEndpoint<?>> allEndpoints = new ArrayList<>();
        Collection<ExposableWebEndpoint> webEndpoints = webEndpointsSupplier.getEndpoints();
        allEndpoints.addAll(webEndpoints);

        String basePath = webEndpointProperties.getBasePath();
        EndpointMapping endpointMapping = new EndpointMapping(basePath);

        boolean shouldRegisterLinksMapping = this.shouldRegisterLinksMapping(webEndpointProperties, environment, basePath);
        return new WebMvcEndpointHandlerMapping(endpointMapping, webEndpoints, endpointMediaTypes, corsProperties.toCorsConfiguration(),
                new EndpointLinksResolver(allEndpoints, basePath), shouldRegisterLinksMapping);
    }

    private boolean shouldRegisterLinksMapping(WebEndpointProperties webEndpointProperties, Environment environment, String basePath) {
        // Since ManagementPortType is no longer available, we manually check for different management port
        String managementPort = environment.getProperty("management.server.port");
        String serverPort = environment.getProperty("server.port");
        return webEndpointProperties.getDiscovery().isEnabled() &&
                (StringUtils.hasText(basePath) || (managementPort != null && !managementPort.equals(serverPort)));
    }
}