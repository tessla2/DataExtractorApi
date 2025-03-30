package io.github.tessla2.dataextractorapi;

import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class DataPdfApplication {

	public static void main(String[] args) {
		//SpringApplication.run(DadosPdfApplication.class, args);

		SpringApplicationBuilder builder = new SpringApplicationBuilder(DataPdfApplication.class);

		builder.bannerMode(Banner.Mode.OFF);

		builder.run(args);

	}

}
