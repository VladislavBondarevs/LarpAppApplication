package de.larphelden.larp_app;

import de.larphelden.larp_app.services.StorageProperties;
import de.larphelden.larp_app.services.StorageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class LarpAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(LarpAppApplication.class, args);
	}

	@Bean
	CommandLineRunner init(StorageService storageService){
		return (args) ->{
			storageService.init();
		};
	}
}
