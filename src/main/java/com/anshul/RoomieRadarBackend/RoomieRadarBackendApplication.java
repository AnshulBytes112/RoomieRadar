package com.anshul.RoomieRadarBackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@SpringBootApplication
public class RoomieRadarBackendApplication {

	public static void main(String[] args) {

		SpringApplication.run(RoomieRadarBackendApplication.class, args);
	}

	@Bean
	public PlatformTransactionManager anyname(MongoDatabaseFactory dbfactory){
		return new MongoTransactionManager(dbfactory);


	}

}
