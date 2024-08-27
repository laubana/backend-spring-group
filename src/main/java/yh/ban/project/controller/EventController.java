package yh.ban.project.controller;

import java.util.List;

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import yh.ban.project.dto.EventDto;
import yh.ban.project.helper.StringHelper;
import yh.ban.project.mapper.EventMapper;
import yh.ban.project.model.Event;
import yh.ban.project.type.Response;

@RestController
@RequestMapping("/api")
public class EventController {
	private static final Logger logger = LogManager.getLogger(CategoryController.class);

	@Autowired
	MongoTemplate mongoTemplate;

	@GetMapping("events")
	public ResponseEntity<Response> getAllEvents() {
		try {
			Aggregation aggregation = Aggregation
					.newAggregation(Aggregation.lookup("category", "category", "_id", "category"));
			AggregationResults<Event> results = mongoTemplate.aggregate(aggregation, "event", Event.class);
			logger.debug(results.getMappedResults().get(0).toString());

			List<Event> existingEvents = mongoTemplate.findAll(Event.class);

			return ResponseEntity.ok().body(new Response("", results.getMappedResults()));
		} catch (Exception exception) {
			exception.printStackTrace();

			return ResponseEntity.internalServerError().body(new Response("Server Error"));
		}
	}

	@PostMapping("event")
	public ResponseEntity<Response> addEvent(@RequestBody EventDto body) {
		try {
			if (StringHelper.isNullOrBlank(body.getCategoryId())) {
				return ResponseEntity.badRequest().body(new Response("Invalid Input"));
			}

			Event existingEvent = mongoTemplate
					.findOne(new Query().addCriteria(Criteria.where("name").is(body.getName())), Event.class);

			if (existingEvent != null) {
				return ResponseEntity.status(HttpStatus.CONFLICT).body(new Response("Event already exists."));
			}

			Event newEvent = mongoTemplate.insert(EventMapper.INSTANCE.eventDtoToEvent(body));

			return ResponseEntity.created(null).body(new Response("Event created successfully.", newEvent));
		} catch (Exception exception) {
			exception.printStackTrace();

			return ResponseEntity.internalServerError().body(new Response("Server Error"));
		}
	}
}
