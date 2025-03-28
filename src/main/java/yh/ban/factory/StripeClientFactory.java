package yh.ban.factory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.stripe.StripeClient;

@Component
public class StripeClientFactory {
	private static String STRIPE_SECRET;

	private static StripeClient stripeClient;

	private StripeClientFactory() {
	}

	@Value("${stripe-secret}")
	public void setStripeSecret(String stripeSecret) {
		STRIPE_SECRET = stripeSecret;
	}

	public static StripeClient getStripeClient() {
		if (stripeClient == null) {
			synchronized (StripeClientFactory.class) {
				if (stripeClient == null) {
					stripeClient = generateStripeClient();
				}
			}
		}
		return stripeClient;
	}

	private static StripeClient generateStripeClient() {
		StripeClient stripeClient = new StripeClient(STRIPE_SECRET);

		return stripeClient;
	}
}
