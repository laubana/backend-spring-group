package yh.ban.project.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import yh.ban.project.dto.EventDto;
import yh.ban.project.dto.RegistrationDto;
import yh.ban.project.helper.StringHelper;
import yh.ban.project.mapper.EventMapper;
import yh.ban.project.mapper.RegistrationMapper;
import yh.ban.project.model.Event;
import yh.ban.project.type.Response;

@RestController
@RequestMapping("/api")
public class EventController {
	private static final Logger logger = LogManager.getLogger(CategoryController.class);

	@Autowired
	MongoTemplate mongoTemplate;

	@PostMapping("/event")
	public ResponseEntity<Response> addEvent(@RequestBody EventDto body, HttpServletRequest httpServletRequest) {
		try {
			String userId = (String) httpServletRequest.getAttribute("id");

			if (StringHelper.isNullOrBlank(body.getAddress()) || StringHelper.isNullOrBlank(body.getCategoryId())
					|| StringHelper.isNullOrBlank(body.getDescription())
					|| StringHelper.isNullOrBlank(body.getImageUrl()) || StringHelper.isNullOrBlank(body.getLatitude())
					|| StringHelper.isNullOrBlank(body.getLongitude()) || StringHelper.isNullOrBlank(body.getName())
					|| StringHelper.isNullOrBlank(body.getThumbnailUrl())) {
				return ResponseEntity.badRequest().body(new Response("Invalid Input"));
			}
			if (StringHelper.isNullOrBlank(userId)) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response("Unauthorized"));
			}

			Event existingEvent = mongoTemplate
					.findOne(new Query().addCriteria(Criteria.where("name").is(body.getName())), Event.class);

			if (existingEvent != null) {
				return ResponseEntity.status(HttpStatus.CONFLICT).body(new Response("Event already exists."));
			}

			body.setUserId(userId);

			Event newEvent = mongoTemplate.insert(EventMapper.INSTANCE.eventDtoToEvent(body));
			newEvent.setCategory(newEvent.getCategory().toString());
			newEvent.setUser(newEvent.getUser().toString());

			mongoTemplate.insert(RegistrationMapper.INSTANCE
					.registrationDtoToRegistration(new RegistrationDto(null, newEvent.get_id(), userId, null, null)));

			return ResponseEntity.created(null).body(new Response("Event created successfully.", newEvent));
		} catch (Exception exception) {
			exception.printStackTrace();

			return ResponseEntity.internalServerError().body(new Response("Server Error"));
		}
	}

	@GetMapping("/events")
	public ResponseEntity<Response> getAllEvents() {
		try {
			Aggregation aggregation = Aggregation.newAggregation(
					Aggregation.lookup("category", "category", "_id", "category"), Aggregation.unwind("category"),
					Aggregation.lookup("user", "user", "_id", "user"), Aggregation.unwind("user"));
			AggregationResults<Event> results = mongoTemplate.aggregate(aggregation, "event", Event.class);

			return ResponseEntity.ok().body(new Response("", results.getMappedResults()));
		} catch (Exception exception) {
			exception.printStackTrace();

			return ResponseEntity.internalServerError().body(new Response("Server Error"));
		}
	}

	@GetMapping("/event/{eventId}")
	public ResponseEntity<Response> getEvent(@PathVariable String eventId) {
		try {
			if (StringHelper.isNullOrBlank(eventId)) {
				return ResponseEntity.badRequest().body(new Response("Invalid Input"));
			}

			Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(Criteria.where("_id").is(eventId)),
					Aggregation.lookup("category", "category", "_id", "category"), Aggregation.unwind("category"),
					Aggregation.lookup("user", "user", "_id", "user"), Aggregation.unwind("user"));
			AggregationResults<Event> results = mongoTemplate.aggregate(aggregation, "event", Event.class);

			return ResponseEntity.ok().body(new Response("", results.getMappedResults().getFirst()));
		} catch (Exception exception) {
			exception.printStackTrace();

			return ResponseEntity.internalServerError().body(new Response("Server Error"));
		}
	}
}
