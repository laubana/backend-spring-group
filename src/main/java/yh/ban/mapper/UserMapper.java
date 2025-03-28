package yh.ban.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import yh.ban.dto.UserDto;
import yh.ban.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
	UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

	User userDtoToUser(UserDto userDto);
}
