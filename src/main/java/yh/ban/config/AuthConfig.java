package yh.ban.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import yh.ban.filter.AuthFilter;

@Configuration
@EnableWebSecurity
public class AuthConfig {
	@Bean
	SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
		CorsConfiguration corsConfiguration = new CorsConfiguration();
		corsConfiguration.addAllowedOrigin("http://localhost:3000");
		corsConfiguration.setAllowedMethods(Arrays.asList("*"));
		corsConfiguration.setAllowedHeaders(Arrays.asList("*"));
		corsConfiguration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
		urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);

		httpSecurity.cors(cors -> cors.configurationSource(urlBasedCorsConfigurationSource));

		httpSecurity.csrf(csrf -> csrf.disable());

		httpSecurity.authorizeHttpRequests(authority -> authority.anyRequest().permitAll());

		httpSecurity.addFilterBefore(new AuthFilter(), UsernamePasswordAuthenticationFilter.class);

		return httpSecurity.build();
	}
}
