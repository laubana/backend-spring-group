package yh.ban.project.converter;

import org.bson.types.ObjectId;
import org.springframework.core.convert.converter.Converter;

import yh.ban.project.model.User;

public class UserConverter implements Converter<ObjectId, User> {
	@Override
	public User convert(ObjectId objectId) {
		User user = new User();
		user.set_id(objectId.toString());

		return user;
	}
}
