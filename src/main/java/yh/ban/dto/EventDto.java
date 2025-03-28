package yh.ban.dto;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class EventDto {
	private String _id;
	private String categoryId;
	private String userId;
	private String thumbnailUrl;
	private String imageUrl;
	private String name;
	private String address;
	private String latitude;
	private String longitude;
	private String description;
	private Instant createdAt;
	private Instant updatedAt;
}
