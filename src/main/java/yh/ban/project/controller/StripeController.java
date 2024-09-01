package yh.ban.project.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.PaymentMethodCollection;
import com.stripe.model.SetupIntent;

import jakarta.servlet.http.HttpServletRequest;
import yh.ban.project.dto.PaymentIntentDto;
import yh.ban.project.helper.StringHelper;
import yh.ban.project.helper.StripeHelper;
import yh.ban.project.model.User;
import yh.ban.project.type.Response;

@RestController
@RequestMapping("/stripe")
public class StripeController {
	private static final Logger logger = LogManager.getLogger(StripeController.class);

	@Autowired
	MongoTemplate mongoTemplate;

	@PostMapping("/payment-intent")
	public ResponseEntity<Response> addPaymentIntent(@RequestBody PaymentIntentDto body,
			HttpServletRequest httpServletRequest) {
		try {
			String userId = (String) httpServletRequest.getAttribute("id");

			if (StringHelper.isNullOrBlank(body.getAmount().toString())) {
				return ResponseEntity.badRequest().body(new Response("Invalid Input"));
			}

			if (StringHelper.isNullOrBlank(userId)) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response("Unauthorized"));
			}

			User existingUser = mongoTemplate
					.findOne(new Query().addCriteria(Criteria.where("_id").is(new ObjectId(userId))), User.class);

			PaymentIntent stripePaymentIntent = StripeHelper.createPaymentIntent(body.getAmount(),
					existingUser.getCustomerId(), body.getPaymentMethodId());

			if (!StringHelper.isNullOrBlank(body.getPaymentMethodId())) {
				StripeHelper.confirmPaymentIntent(stripePaymentIntent.getId());
			}

			Map<String, Object> data = new HashMap<>();
			data.put("client_secret", stripePaymentIntent.getClientSecret());
			data.put("id", stripePaymentIntent.getId());

			return ResponseEntity.created(null).body(new Response("Payment intent created successfully.", data));
		} catch (Exception exception) {
			exception.printStackTrace();

			return ResponseEntity.internalServerError().body(new Response("Server Error"));
		}
	}

	@PostMapping("/setup-intent")
	public ResponseEntity<Response> addSetupIntent(HttpServletRequest httpServletRequest) {
		try {
			String userId = (String) httpServletRequest.getAttribute("id");

			if (StringHelper.isNullOrBlank(userId)) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response("Unauthorized"));
			}

			User existingUser = mongoTemplate
					.findOne(new Query().addCriteria(Criteria.where("_id").is(new ObjectId(userId))), User.class);

			SetupIntent stripeSetupIntent = StripeHelper.createSetupIntent(existingUser.getCustomerId());

			Map<String, Object> data = new HashMap<>();
			data.put("client_secret", stripeSetupIntent.getClientSecret());
			data.put("id", stripeSetupIntent.getId());

			return ResponseEntity.created(null).body(new Response("Setup intent created successfully.", data));
		} catch (Exception exception) {
			exception.printStackTrace();

			return ResponseEntity.internalServerError().body(new Response("Server Error"));
		}
	}

	@DeleteMapping("/payment-method/{paymentMethodId}")
	public ResponseEntity<Response> deletePaymentMethod(@PathVariable String paymentMethodId) {
		try {
			if (StringHelper.isNullOrBlank(paymentMethodId)) {
				return ResponseEntity.badRequest().body(new Response("Invalid Input"));
			}

			StripeHelper.removePaymentMethod(paymentMethodId);

			return ResponseEntity.ok().body(new Response("Payment method deleted successfully."));
		} catch (Exception exception) {
			exception.printStackTrace();

			return ResponseEntity.internalServerError().body(new Response("Server Error"));
		}
	}

	@GetMapping("/payment-methods")
	public ResponseEntity<Response> getAllPaymentMethods(HttpServletRequest httpServletRequest) {
		try {
			String userId = (String) httpServletRequest.getAttribute("id");

			if (StringHelper.isNullOrBlank(userId)) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response("Unauthorized"));
			}

			User existingUser = mongoTemplate
					.findOne(new Query().addCriteria(Criteria.where("_id").is(new ObjectId(userId))), User.class);

			PaymentMethodCollection stripePaymentMethodCollection = StripeHelper
					.listPaymentMethods(existingUser.getCustomerId());

			List<PaymentMethod> paymentMethods = stripePaymentMethodCollection.getData();

			List<Map<String, Object>> datas = new ArrayList<>();

			paymentMethods.forEach((paymentMethod) -> {
				Map<String, Object> data = new HashMap<>();

				data.put("brand", paymentMethod.getCard().getBrand());
				data.put("lastDigits", paymentMethod.getCard().getLast4());
				data.put("id", paymentMethod.getId());

				datas.add(data);
			});

			return ResponseEntity.ok().body(new Response("", datas));
		} catch (Exception exception) {
			exception.printStackTrace();

			return ResponseEntity.internalServerError().body(new Response("Server Error"));
		}
	}
}
