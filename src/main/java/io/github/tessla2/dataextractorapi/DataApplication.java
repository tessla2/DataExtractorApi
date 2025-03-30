package io.github.tessla2.dataextractorapi;

import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class DataApplication {

	public static void main(String[] args) {
		//SpringApplication.run(DadosPdfApplication.class, args);

		SpringApplicationBuilder builder = new SpringApplicationBuilder(DataApplication.class);

		builder.bannerMode(Banner.Mode.OFF);

		builder.run(args);

	}

}
