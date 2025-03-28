package yh.ban.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import yh.ban.helper.StringHelper;
import yh.ban.model.User;
import yh.ban.type.Response;

@RestController
@RequestMapping("/api")
public class UserController {
	private static final Logger logger = LogManager.getLogger(CategoryController.class);

	@Autowired
	MongoTemplate mongoTemplate;

	@GetMapping("user/{userId}")
	public ResponseEntity<Response> getUser(@PathVariable String userId) {
		try {
			if (StringHelper.isNullOrBlank(userId)) {
				return ResponseEntity.badRequest().body(new Response("Invalid Input"));
			}

			Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(Criteria.where("_id").is(userId)));
			AggregationResults<User> results = mongoTemplate.aggregate(aggregation, "user", User.class);

			return ResponseEntity.ok().body(new Response("", results.getMappedResults().getFirst()));
		} catch (Exception exception) {
			exception.printStackTrace();

			return ResponseEntity.internalServerError().body(new Response("Server Error"));
		}
	}
}
