package yh.ban.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
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

import com.mongodb.client.result.DeleteResult;

import jakarta.servlet.http.HttpServletRequest;
import yh.ban.dto.RegistrationDto;
import yh.ban.helper.StringHelper;
import yh.ban.mapper.RegistrationMapper;
import yh.ban.model.Event;
import yh.ban.model.Registration;
import yh.ban.type.Response;

@RestController
@RequestMapping("/api")
public class RegistrationController {
	private static final Logger logger = LogManager.getLogger(CategoryController.class);

	@Autowired
	MongoTemplate mongoTemplate;

	@PostMapping("/registration")
	public ResponseEntity<Response> addRegistration(@RequestBody RegistrationDto body,
			HttpServletRequest httpServletRequest) {
		try {
			String userId = (String) httpServletRequest.getAttribute("id");

			if (StringHelper.isNullOrBlank(body.getEventId())) {
				return ResponseEntity.badRequest().body(new Response("Invalid Input"));
			}

			if (StringHelper.isNullOrBlank(userId)) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response("Unauthorized"));
			}

			Registration existingRegistration = mongoTemplate.findOne(new Query().addCriteria(Criteria.where("event")
					.is(new ObjectId(body.getEventId())).andOperator(Criteria.where("user").is(new ObjectId(userId)))),
					Registration.class);

			if (existingRegistration != null) {
				return ResponseEntity.status(HttpStatus.CONFLICT).body(new Response("Registration already exists."));
			}

			body.setUserId(userId);

			Registration newRegistration = mongoTemplate
					.insert(RegistrationMapper.INSTANCE.registrationDtoToRegistration(body));

			newRegistration.setEvent(newRegistration.getEvent().toString());
			newRegistration.setUser(newRegistration.getUser().toString());

			return ResponseEntity.created(null)
					.body(new Response("Registration created successfully.", newRegistration));
		} catch (Exception exception) {
			exception.printStackTrace();

			return ResponseEntity.internalServerError().body(new Response("Server Error"));
		}
	}

	@GetMapping("/registrations/{eventId}")
	public ResponseEntity<Response> getRegistrations(@PathVariable String eventId) {
		try {
			if (StringHelper.isNullOrBlank(eventId)) {
				return ResponseEntity.badRequest().body(new Response("Invalid Input"));
			}

			Aggregation aggregation = Aggregation.newAggregation(
					Aggregation.match(Criteria.where("event").is(new ObjectId(eventId))),
					Aggregation.lookup("event", "event", "_id", "event"), Aggregation.unwind("event"),
					Aggregation.lookup("user", "user", "_id", "user"), Aggregation.unwind("user"));
			AggregationResults<Registration> results = mongoTemplate.aggregate(aggregation, "registration",
					Registration.class);

			results.forEach((result) -> {
				Event event = (Event) result.getEvent();

				event.setCategory(event.getCategory().toString());
				event.setUser(event.getUser().toString());
			});

			return ResponseEntity.ok().body(new Response("", results.getMappedResults()));
		} catch (Exception exception) {
			exception.printStackTrace();

			return ResponseEntity.internalServerError().body(new Response("Server Error"));
		}
	}

	@GetMapping("/registration/{eventId}")
	public ResponseEntity<Response> getRegistration(@PathVariable String eventId,
			HttpServletRequest httpServletRequest) {
		try {
			String userId = (String) httpServletRequest.getAttribute("id");

			if (StringHelper.isNullOrBlank(eventId)) {
				return ResponseEntity.badRequest().body(new Response("Invalid Input"));
			}

			if (StringHelper.isNullOrBlank(userId)) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response("Unauthorized"));
			}

			Aggregation aggregation = Aggregation.newAggregation(
					Aggregation.match(Criteria.where("event").is(new ObjectId(eventId))
							.andOperator(Criteria.where("user").is(new ObjectId(userId)))),
					Aggregation.lookup("event", "event", "_id", "event"), Aggregation.unwind("event"),
					Aggregation.lookup("user", "user", "_id", "user"), Aggregation.unwind("user"));
			AggregationResults<Registration> results = mongoTemplate.aggregate(aggregation, "registration",
					Registration.class);

			results.forEach((result) -> {
				Event event = (Event) result.getEvent();

				event.setCategory(event.getCategory().toString());
				event.setUser(event.getUser().toString());
			});

			if (0 < results.getMappedResults().size()) {
				return ResponseEntity.ok().body(new Response("", results.getMappedResults().getFirst()));
			} else {
				return ResponseEntity.ok().body(new Response(""));
			}
		} catch (Exception exception) {
			exception.printStackTrace();

			return ResponseEntity.internalServerError().body(new Response("Server Error"));
		}
	}

	@DeleteMapping("/registration/{registrationId}")
	public ResponseEntity<Response> deleteRegistration(@PathVariable String registrationId,
			HttpServletRequest httpServletRequest) {
		try {
			String userId = (String) httpServletRequest.getAttribute("id");

			if (StringHelper.isNullOrBlank(registrationId)) {
				return ResponseEntity.badRequest().body(new Response("Invalid Input"));
			}

			if (StringHelper.isNullOrBlank(userId)) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response("Unauthorized"));
			}

			DeleteResult deleteResult = mongoTemplate.remove(new Query().addCriteria(Criteria.where("_id")
					.is(new ObjectId(registrationId)).andOperator(Criteria.where("user").is(new ObjectId(userId)))),
					Registration.class);

			return ResponseEntity.ok()
					.body(new Response("Registration deleted successfully.", deleteResult.getDeletedCount()));
		} catch (Exception exception) {
			exception.printStackTrace();

			return ResponseEntity.internalServerError().body(new Response("Server Error"));
		}
	}
}
