package yh.ban.model;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Document(collection = "transaction")
@Getter
@Setter
@ToString
public class Transaction {
	@Id
	private String _id;
	private Long amount;
	private String chargeId;
	private String description;
	private String receiptUrl;
	private Object user;
	@CreatedDate
	private Instant createdAt;
	@LastModifiedDate
	private Instant updatedAt;
}
