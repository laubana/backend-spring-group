package yh.ban.project.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
}
