package yh.ban.project.converter;

import org.bson.types.ObjectId;
import org.springframework.core.convert.converter.Converter;

public class CategoryConverter implements Converter<ObjectId, Object> {
	@Override
	public Object convert(ObjectId objectId) {
		return objectId;
	}
}
