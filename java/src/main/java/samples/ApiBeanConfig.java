package samples;

import io.swagger.client.ApiClient;
import io.swagger.client.api.SeedApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiBeanConfig {

    @Bean
    public SeedApi seedApi() {
        return new SeedApi(apiClient());
    }

    @Bean
    public ApiClient apiClient() {
        return new ApiClient();
    }
}
