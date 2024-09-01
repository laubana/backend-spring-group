package yh.ban.project.dto;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ImageDto {
	private String _id;
	private String eventId;
	private String userId;
	private String imageUrl;
	private Instant createdAt;
	private Instant updatedAt;
}
