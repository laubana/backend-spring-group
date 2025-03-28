package yh.ban.webhook;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import yh.ban.dto.EventDto;
import yh.ban.helper.StringHelper;
import yh.ban.mapper.EventMapper;
import yh.ban.model.Event;
import yh.ban.type.Response;

@RestController
@RequestMapping("/webhook")
public class StripeWebhook {
	private static final Logger logger = LogManager.getLogger(StripeWebhook.class);

	@Autowired
	MongoTemplate mongoTemplate;

	@PostMapping("stripe")
	public ResponseEntity<Response> stripe(@RequestBody EventDto body, HttpServletRequest httpServletRequest) {
		try {
			String userId = (String) httpServletRequest.getAttribute("id");
			if (StringHelper.isNullOrBlank(body.getAddress()) || StringHelper.isNullOrBlank(body.getCategoryId())
					|| StringHelper.isNullOrBlank(body.getDescription())
					|| StringHelper.isNullOrBlank(body.getImageUrl()) || StringHelper.isNullOrBlank(body.getLatitude())
					|| StringHelper.isNullOrBlank(body.getLongitude()) || StringHelper.isNullOrBlank(body.getName())
					|| StringHelper.isNullOrBlank(body.getThumbnailUrl())) {
				return ResponseEntity.badRequest().body(new Response("Invalid Input"));
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

			return ResponseEntity.created(null).body(new Response("Event created successfully.", newEvent));
		} catch (Exception exception) {
			exception.printStackTrace();

			return ResponseEntity.internalServerError().body(new Response("Server Error"));
		}
	}
}
