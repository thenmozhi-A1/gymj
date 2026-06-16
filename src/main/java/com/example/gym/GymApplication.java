package com.example.gym;

import com.example.gym.config.NotificationSettings;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(NotificationSettings.class)
public class GymApplication {

	public static void main(String[] args) {
		// Trigger Render Deploy
		SpringApplication.run(GymApplication.class, args);
	}

}
