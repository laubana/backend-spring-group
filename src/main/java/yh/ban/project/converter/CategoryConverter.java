package yh.ban.project.converter;

import org.bson.types.ObjectId;
import org.springframework.core.convert.converter.Converter;

import yh.ban.project.model.Category;

public class CategoryConverter implements Converter<ObjectId, Category> {
	@Override
	public Category convert(ObjectId objectId) {
		Category category = new Category();
		category.set_id(objectId.toString());

		return category;
	}
}
