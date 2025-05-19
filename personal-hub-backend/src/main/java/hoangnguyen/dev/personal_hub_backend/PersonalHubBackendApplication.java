package hoangnguyen.dev.personal_hub_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class PersonalHubBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(PersonalHubBackendApplication.class, args);
	}

}
