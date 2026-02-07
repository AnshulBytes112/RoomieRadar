package com.anshul.RoomieRadarBackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import org.springframework.transaction.PlatformTransactionManager;

import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class RoomieRadarBackendApplication {

	public static void main(String[] args) {

		SpringApplication.run(RoomieRadarBackendApplication.class, args);
	}

}
