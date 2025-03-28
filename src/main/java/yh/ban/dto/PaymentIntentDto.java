package yh.ban.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PaymentIntentDto {
	private Long amount;
	private String paymentMethodId;
}
