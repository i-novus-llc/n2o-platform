package net.n2oapp.platform.initializr;

import io.spring.initializr.metadata.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class InitializrApplication {

	public static void main(String[] args) {
		SpringApplication.run(InitializrApplication.class, args);
	}

	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	public InitializrMetadataProvider initializrMetadataProvider(
			InitializrProperties properties, RestTemplate restTemplate) {
		InitializrMetadata metadata = InitializrMetadataBuilder
				.fromInitializrProperties(properties).build();
		return new N2oInitializrMetadataProvider(metadata, restTemplate);
	}
}
