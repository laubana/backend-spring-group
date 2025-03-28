package yh.ban.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import yh.ban.converter.CategoryConverter;

@Configuration
@EnableMongoAuditing
@EnableMongoRepositories
public class DBConfig {
	@Bean
	MongoCustomConversions customConversions() {
		return new MongoCustomConversions(Arrays.asList(new CategoryConverter()));
	}
}
