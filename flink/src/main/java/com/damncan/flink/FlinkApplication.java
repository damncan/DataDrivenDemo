package com.damncan.flink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

/**
 * This application provide a data streaming app built in flink.
 *
 * @author Ian Zhong (damncan)
 * @since 14 January 2023
 */
@SpringBootApplication(scanBasePackages = "com.damncan.flink")
@PropertySources({
		@PropertySource("classpath:application.properties")
})
public class FlinkApplication {
	public static void main(String[] args) {
		SpringApplication.run(FlinkApplication.class, args);
	}
}
