package com.udsilva.minhasfinancas.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableWebMvc
@Configuration
public class WebConfiguration implements WebMvcConfigurer{
	
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		
	   // permite o acesso a aplicacao vindo de qualquer servidor, podem ser executados
		// os metods definidos em .allowedMethods  	
	   registry.addMapping("/**").allowedMethods("GET", "PUT", "POST", "DELETE", "OPTIONS");
	}
}
