package yh.ban.helper;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethodCollection;
import com.stripe.model.Refund;
import com.stripe.model.SetupIntent;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentIntentCreateParams.AutomaticPaymentMethods;
import com.stripe.param.PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.RefundCreateParams.Reason;

import yh.ban.factory.StripeClientFactory;

import com.stripe.param.SetupIntentCreateParams;

public class StripeHelper {
	public static PaymentIntent confirmPaymentIntent(String paymentIntentId) throws StripeException {
		StripeClient stripeClient = StripeClientFactory.getStripeClient();
		PaymentIntent paymentIntent = stripeClient.paymentIntents().confirm(paymentIntentId);

		return paymentIntent;
	}

	public static Customer createCustomer(String email, String name) throws StripeException {
		CustomerCreateParams customerCreateParams = CustomerCreateParams.builder().setEmail(email).setName(name)
				.build();
		StripeClient stripeClient = StripeClientFactory.getStripeClient();
		Customer customer = stripeClient.customers().create(customerCreateParams);

		return customer;
	}

	public static PaymentIntent createPaymentIntent(Long amount, String customerId, String paymentMethodId)
			throws StripeException {
		AutomaticPaymentMethods automaticPaymentMethod = AutomaticPaymentMethods.builder()
				.setAllowRedirects(AllowRedirects.NEVER).setEnabled(true).build();
		PaymentIntentCreateParams paymentIntentCreateParams = PaymentIntentCreateParams.builder().setAmount(amount)
				.setCurrency("cad").setCustomer(customerId).setPaymentMethod(paymentMethodId)
				.setAutomaticPaymentMethods(automaticPaymentMethod).build();
		StripeClient stripeClient = StripeClientFactory.getStripeClient();
		PaymentIntent paymentIntent = stripeClient.paymentIntents().create(paymentIntentCreateParams);

		return paymentIntent;
	}

	public static Refund createRefund(String chargeId) throws StripeException {
		RefundCreateParams refundCreateParams = RefundCreateParams.builder().setCharge(chargeId)
				.setReason(Reason.REQUESTED_BY_CUSTOMER).build();
		StripeClient stripeClient = StripeClientFactory.getStripeClient();
		Refund refund = stripeClient.refunds().create(refundCreateParams);

		return refund;
	}

	public static SetupIntent createSetupIntent(String customerId) throws StripeException {
		SetupIntentCreateParams setupIntentCreateParams = SetupIntentCreateParams.builder().setCustomer(customerId)
				.build();
		StripeClient stripeClient = StripeClientFactory.getStripeClient();
		SetupIntent setupIntent = stripeClient.setupIntents().create(setupIntentCreateParams);

		return setupIntent;
	}

	public static Charge getCharge(String chargeId) throws StripeException {
		StripeClient stripeClient = StripeClientFactory.getStripeClient();
		Charge charge = stripeClient.charges().retrieve(chargeId);

		return charge;
	}

	public static PaymentIntent getPaymentIntent(String paymentIntentId) throws StripeException {
		StripeClient stripeClient = StripeClientFactory.getStripeClient();
		PaymentIntent paymentIntent = stripeClient.paymentIntents().retrieve(paymentIntentId);

		return paymentIntent;
	}

	public static PaymentMethodCollection listPaymentMethods(String customerId) throws StripeException {
		StripeClient stripeClient = StripeClientFactory.getStripeClient();
		PaymentMethodCollection paymentMethodCollection = stripeClient.customers().retrieve(customerId)
				.listPaymentMethods();

		return paymentMethodCollection;
	}

	public static void removePaymentMethod(String paymentMethodId) throws StripeException {
		StripeClient stripeClient = StripeClientFactory.getStripeClient();
		stripeClient.paymentMethods().detach(paymentMethodId);
	}
}
