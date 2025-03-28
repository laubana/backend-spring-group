package yh.ban.dto;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CategoryDto {
	private String _id;
	private String value;
	private Instant createdAt;
	private Instant updatedAt;
}
