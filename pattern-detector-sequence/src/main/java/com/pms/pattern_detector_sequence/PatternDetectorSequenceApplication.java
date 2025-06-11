package com.pms.pattern_detector_sequence;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;

@SpringBootApplication
@EnableRabbit
public class PatternDetectorSequenceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PatternDetectorSequenceApplication.class, args);
	}

}
