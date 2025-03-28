package yh.ban.dto;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TransactionDto {
	private String _id;
	private Long amount;
	private String chargeId;
	private String description;
	private String paymentIntentId;
	private String receiptUrl;
	private String userId;
	private Instant createdAt;
	private Instant updatedAt;
}
