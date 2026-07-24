package com.memoria.Memoria;

import com.memoria.Memoria.config.SearchProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(SearchProperties.class)
public class MemoriaApplication {

	public static void main(String[] args) {
		SpringApplication.run(MemoriaApplication.class, args);
	}

}
