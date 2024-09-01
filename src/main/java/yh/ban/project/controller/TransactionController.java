package yh.ban.project.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stripe.model.Charge;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;

import jakarta.servlet.http.HttpServletRequest;
import yh.ban.project.dto.TransactionDto;
import yh.ban.project.helper.StringHelper;
import yh.ban.project.helper.StripeHelper;
import yh.ban.project.mapper.TransactionMapper;
import yh.ban.project.model.Transaction;
import yh.ban.project.type.Response;

@RestController
@RequestMapping("/api")
public class TransactionController {
	private static final Logger logger = LogManager.getLogger(TransactionController.class);

	@Autowired
	MongoTemplate mongoTemplate;

	@PostMapping("/transaction")
	public ResponseEntity<Response> addTransaction(@RequestBody TransactionDto body,
			HttpServletRequest httpServletRequest) {
		try {
			String userId = (String) httpServletRequest.getAttribute("id");

			if (StringHelper.isNullOrBlank(body.getDescription())
					|| StringHelper.isNullOrBlank(body.getPaymentIntentId())) {
				return ResponseEntity.badRequest().body(new Response("Invalid Input"));
			}

			if (StringHelper.isNullOrBlank(userId)) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response("Unauthorized"));
			}

			PaymentIntent stripePaymentIntent = StripeHelper.getPaymentIntent(body.getPaymentIntentId());

			String chargeId = stripePaymentIntent.getLatestCharge();

			Charge charge = StripeHelper.getCharge(chargeId);

			body.setAmount(charge.getAmount());
			body.setChargeId(charge.getId());
			body.setReceiptUrl(charge.getReceiptUrl());
			body.setUserId(userId);

			Transaction newTransaction = mongoTemplate
					.insert(TransactionMapper.INSTANCE.transactionDtoToTransaction(body));

			newTransaction.setUser(newTransaction.getUser().toString());

			return ResponseEntity.created(null).body(new Response("Transaction created successfully.", newTransaction));
		} catch (Exception exception) {
			exception.printStackTrace();

			return ResponseEntity.internalServerError().body(new Response("Server Error"));
		}
	}

	@DeleteMapping("/transaction/{transactionId}")
	public ResponseEntity<Response> deleteComment(@PathVariable String transactionId,
			HttpServletRequest httpServletRequest) {
		try {
			if (StringHelper.isNullOrBlank(transactionId)) {
				return ResponseEntity.badRequest().body(new Response("Invalid Input"));
			}

			Transaction existingTransaction = mongoTemplate
					.findOne(new Query().addCriteria(Criteria.where("_id").is(transactionId)), Transaction.class);

			Refund stripeRefund = StripeHelper.createRefund(existingTransaction.getChargeId());

			mongoTemplate.findAndModify(new Query().addCriteria(Criteria.where("_id").is(transactionId)),
					new Update().inc("amount", -stripeRefund.getAmount()), Transaction.class);

			return ResponseEntity.ok().body(new Response("Transaction deleted successfully."));
		} catch (Exception exception) {
			exception.printStackTrace();

			return ResponseEntity.internalServerError().body(new Response("Server Error"));
		}
	}

	@GetMapping("/transactions")
	public ResponseEntity<Response> getTransactions(HttpServletRequest httpServletRequest) {
		try {
			String userId = (String) httpServletRequest.getAttribute("id");

			if (StringHelper.isNullOrBlank(userId)) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response("Unauthorized"));
			}

			Aggregation aggregation = Aggregation.newAggregation(
					Aggregation.match(Criteria.where("user").is(new ObjectId(userId))),
					Aggregation.lookup("user", "user", "_id", "user"), Aggregation.unwind("user"));
			AggregationResults<Transaction> results = mongoTemplate.aggregate(aggregation, "transaction",
					Transaction.class);

			return ResponseEntity.ok().body(new Response("", results.getMappedResults()));
		} catch (Exception exception) {
			exception.printStackTrace();

			return ResponseEntity.internalServerError().body(new Response("Server Error"));
		}
	}
}
