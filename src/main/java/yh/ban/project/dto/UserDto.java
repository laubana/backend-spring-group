package yh.ban.project.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class UserDto {
	private String _id;
	private String email;
	private String password;
	private String imageUrl;
	private String name;
	private String address;
	private Long latitude;
	private Long longitude;
	private String description;
	private String customerId;
	private Instant createdAt;
	private Instant updatedAt;
}
