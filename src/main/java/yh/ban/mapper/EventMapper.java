package yh.ban.mapper;

import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import yh.ban.dto.EventDto;
import yh.ban.helper.StringHelper;
import yh.ban.model.Event;

@Mapper(componentModel = "spring")
public interface EventMapper {
	EventMapper INSTANCE = Mappers.getMapper(EventMapper.class);

	@Mapping(source = "categoryId", target = "category", qualifiedByName = "categoryIdToCategory")
	@Mapping(source = "userId", target = "user", qualifiedByName = "userIdToUser")
	Event eventDtoToEvent(EventDto eventDto);

	@Named("categoryIdToCategory")
	default ObjectId categoryIdToCategory(String categoryId) {
		if (StringHelper.isNullOrBlank(categoryId)) {
			return null;
		}

		ObjectId category = new ObjectId(categoryId);

		return category;
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
