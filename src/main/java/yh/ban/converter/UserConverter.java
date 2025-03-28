package yh.ban.converter;

import org.bson.types.ObjectId;
import org.springframework.core.convert.converter.Converter;

import yh.ban.model.User;

public class UserConverter implements Converter<ObjectId, User> {
	@Override
	public User convert(ObjectId objectId) {
		System.out.println("user converter");
		User user = new User();
		user.set_id(objectId.toString());

		return user;
	}
}
