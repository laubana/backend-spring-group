package yh.ban.project.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import yh.ban.project.dto.EventDto;
import yh.ban.project.helper.StringHelper;
import yh.ban.project.model.Category;
import yh.ban.project.model.Event;
import yh.ban.project.model.User;

@Mapper(componentModel = "spring")
public interface EventMapper {
	EventMapper INSTANCE = Mappers.getMapper(EventMapper.class);

	@Mapping(source = "categoryId", target = "category", qualifiedByName = "categoryIdToCategory")
	@Mapping(source = "userId", target = "user", qualifiedByName = "userIdToUser")
	Event eventDtoToEvent(EventDto eventDto);

	@Named("categoryIdToCategory")
	default Category categoryIdToCategory(String categoryId) {
		if (StringHelper.isNullOrBlank(categoryId)) {
			return null;
		}

		Category category = new Category();
		category.set_id(categoryId);

		return category;
	}

	@Named("userIdToUser")
	default User userIdToUser(String userId) {
		if (StringHelper.isNullOrBlank(userId)) {
			return null;
		}

		User user = new User();
		user.set_id(userId);

		return user;
	}
}
