package com.example.easy_learning;

import com.example.easy_learning.service.props.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class EasyLearningApplication {
	public static void main(String[] args) {
		SpringApplication.run(EasyLearningApplication.class, args);
	}
}


