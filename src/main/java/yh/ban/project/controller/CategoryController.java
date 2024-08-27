package yh.ban.project.controller;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import yh.ban.project.dto.CategoryDto;
import yh.ban.project.helper.StringHelper;
import yh.ban.project.mapper.CategoryMapper;
import yh.ban.project.model.Category;
import yh.ban.project.type.Response;

@RestController
@RequestMapping("/api")
public class CategoryController {
	private static final Logger logger = LogManager.getLogger(CategoryController.class);

	@Autowired
	MongoTemplate mongoTemplate;

	@GetMapping("categorys")
	public ResponseEntity<Response> getAllCategories() {
		try {
			List<Category> categories = mongoTemplate.findAll(Category.class);

			return ResponseEntity.ok().body(new Response("", categories));
		} catch (Exception exception) {
			exception.printStackTrace();

			return ResponseEntity.internalServerError().body(new Response("Server Error"));
		}
	}

	@PostMapping("/category")
	public ResponseEntity<Response> addCategory(@RequestBody CategoryDto body) {
		try {
			if (StringHelper.isNullOrBlank(body.getValue())) {
				return ResponseEntity.badRequest().body(new Response("Invalid Input"));
			}

			Category existingCategory = mongoTemplate
					.findOne(new Query().addCriteria(Criteria.where("value").is(body.getValue())), Category.class);

			if (existingCategory != null) {
				return ResponseEntity.status(HttpStatus.CONFLICT).body(new Response("Category already exists."));
			}

			Category newCategory = mongoTemplate.insert(CategoryMapper.INSTANCE.categoryDtoToCategory(body));

			return ResponseEntity.created(null).body(new Response("Category created successfully", newCategory));
		} catch (Exception exception) {
			exception.printStackTrace();

			return ResponseEntity.internalServerError().body(new Response("Server Error"));
		}
	}
}
