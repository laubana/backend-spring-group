package yh.ban.project.mapper;

import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import yh.ban.project.dto.CommentDto;
import yh.ban.project.helper.StringHelper;
import yh.ban.project.model.Comment;

@Mapper
public interface CommentMapper {
	CommentMapper INSTANCE = Mappers.getMapper(CommentMapper.class);

	@Mapping(source = "eventId", target = "event", qualifiedByName = "eventIdToEvent")
	@Mapping(source = "userId", target = "user", qualifiedByName = "userIdToUser")
	Comment commentDtoToComment(CommentDto commentDto);

	@Named("eventIdToEvent")
	default ObjectId eventIdToEvent(String eventId) {
		if (StringHelper.isNullOrBlank(eventId)) {
			return null;
		}

		ObjectId event = new ObjectId(eventId);

		return event;
	}

	@Named("userIdToUser")
	default ObjectId userIdToUser(String userId) {
		if (StringHelper.isNullOrBlank(userId)) {
			return null;
		}

		ObjectId user = new ObjectId(userId);

		return user;
	}
}
