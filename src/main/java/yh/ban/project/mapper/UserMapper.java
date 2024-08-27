package yh.ban.project.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import yh.ban.project.dto.UserDto;
import yh.ban.project.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
	UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

	User userDtoToUser(UserDto userDto);
}
