package yh.ban.project.dto;

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
	private Long latitude;
	private Long longitude;
	private String description;
	private Boolean isActive;
}
