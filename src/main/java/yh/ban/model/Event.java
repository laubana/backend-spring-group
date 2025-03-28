package yh.ban.model;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
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
	private Object category;
	private Object user;
	private String thumbnailUrl;
	private String imageUrl;
	private String name;
	private String address;
	private Double latitude;
	private Double longitude;
	private String description;
	@CreatedDate
	private Instant createdAt;
	@LastModifiedDate
	private Instant updatedAt;
}
