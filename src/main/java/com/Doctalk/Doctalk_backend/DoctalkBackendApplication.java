package com.Doctalk.Doctalk_backend;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class DoctalkBackendApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(DoctalkBackendApplication.class)
			.properties("server.port=8090")
			.run(args);
	}

}
