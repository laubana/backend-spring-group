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
import yh.ban.project.dto.ImageDto;
import yh.ban.project.helper.StringHelper;
import yh.ban.project.mapper.ImageMapper;
import yh.ban.project.model.Event;
import yh.ban.project.model.Image;
import yh.ban.project.type.Response;

@RestController
@RequestMapping("/api")
public class ImageController {
	private static final Logger logger = LogManager.getLogger(ImageController.class);

	@Autowired
	MongoTemplate mongoTemplate;

	@PostMapping("/image")
	public ResponseEntity<Response> addImage(@RequestBody ImageDto body, HttpServletRequest httpServletRequest) {
		try {
			String userId = (String) httpServletRequest.getAttribute("id");

			if (StringHelper.isNullOrBlank(body.getEventId()) || StringHelper.isNullOrBlank(body.getImageUrl())) {
				return ResponseEntity.badRequest().body(new Response("Invalid Input"));
			}

			if (StringHelper.isNullOrBlank(userId)) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response("Unauthorized"));
			}

			body.setUserId(userId);

			Image newImage = mongoTemplate.insert(ImageMapper.INSTANCE.imageDtoToImage(body));

			newImage.setEvent(newImage.getEvent().toString());
			newImage.setUser(newImage.getUser().toString());

			return ResponseEntity.created(null).body(new Response("Image created successfully.", newImage));
		} catch (Exception exception) {
			exception.printStackTrace();

			return ResponseEntity.internalServerError().body(new Response("Server Error"));
		}
	}

	@DeleteMapping("/image/{imageId}")
	public ResponseEntity<Response> deleteImage(@PathVariable String imageId, HttpServletRequest httpServletRequest) {
		try {
			String userId = (String) httpServletRequest.getAttribute("id");

			if (StringHelper.isNullOrBlank(imageId)) {
				return ResponseEntity.badRequest().body(new Response("Invalid Input"));
			}

			if (StringHelper.isNullOrBlank(userId)) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response("Unauthorized"));
			}

			DeleteResult deleteResult = mongoTemplate.remove(new Query().addCriteria(Criteria.where("_id")
					.is(new ObjectId(imageId)).andOperator(Criteria.where("user").is(new ObjectId(userId)))),
					Image.class);

			return ResponseEntity.ok()
					.body(new Response("Image deleted successfully.", deleteResult.getDeletedCount()));
		} catch (Exception exception) {
			exception.printStackTrace();

			return ResponseEntity.internalServerError().body(new Response("Server Error"));
		}
	}

	@GetMapping("/images/{eventId}")
	public ResponseEntity<Response> getImages(@PathVariable String eventId) {
		try {
			if (StringHelper.isNullOrBlank(eventId)) {
				return ResponseEntity.badRequest().body(new Response("Invalid Input"));
			}

			Aggregation aggregation = Aggregation.newAggregation(
					Aggregation.match(Criteria.where("event").is(new ObjectId(eventId))),
					Aggregation.lookup("event", "event", "_id", "event"), Aggregation.unwind("event"),
					Aggregation.lookup("user", "user", "_id", "user"), Aggregation.unwind("user"));
			AggregationResults<Image> results = mongoTemplate.aggregate(aggregation, "image", Image.class);

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
}
