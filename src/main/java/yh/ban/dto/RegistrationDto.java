package yh.ban.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class RegistrationDto {
	private String _id;
	private String eventId;
	private String userId;
	private Instant createdAt;
	private Instant updatedAt;
}
