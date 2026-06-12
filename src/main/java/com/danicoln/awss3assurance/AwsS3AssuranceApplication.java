package com.danicoln.awss3assurance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AwsS3AssuranceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AwsS3AssuranceApplication.class, args);
    }
}
