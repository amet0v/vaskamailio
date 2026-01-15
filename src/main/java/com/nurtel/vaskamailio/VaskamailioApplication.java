package com.nurtel.vaskamailio;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

@SpringBootApplication
public class VaskamailioApplication {

	public static void main(String[] args) {

		String envPath = System.getProperty("ENV_FILE");
		System.out.println("ENV_FILE = " + System.getProperty("ENV_FILE"));

		Dotenv dotenv = (envPath != null && !envPath.isBlank())
				? Dotenv.configure()
				.directory(new File(envPath).getParent())
				.filename(new File(envPath).getName())
				.ignoreIfMissing()
				.load()
				: Dotenv.configure()
				.ignoreIfMissing()
				.load();

		dotenv.entries().forEach(e -> {
			if (System.getProperty(e.getKey()) == null && System.getenv(e.getKey()) == null) {
				System.setProperty(e.getKey(), e.getValue());
			}
		});

		SpringApplication.run(VaskamailioApplication.class, args);
	}
}
