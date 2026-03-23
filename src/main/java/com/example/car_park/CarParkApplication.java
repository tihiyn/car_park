package com.example.car_park;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CarParkApplication {

	public static void main(String[] args) {
		SpringApplication.run(CarParkApplication.class, args);
	}

}
