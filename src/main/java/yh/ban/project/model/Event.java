package yh.ban.project.model;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Document(collection = "event")
@Getter
@Setter
@ToString
public class Event {
	@Id
	private String _id;
	@DBRef
	private Category category;
	@DBRef
	private User user;
	private String thumbnailUrl;
	private String imageUrl;
	private String name;
	private String address;
	private Long latitude;
	private Long longitude;
	private String description;
	private Boolean isActive;
	@CreatedDate
	private Instant createdAt;
	@LastModifiedDate
	private Instant updatedAt;
}
