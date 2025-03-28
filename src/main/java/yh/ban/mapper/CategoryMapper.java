package yh.ban.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import yh.ban.dto.CategoryDto;
import yh.ban.model.Category;

@Mapper
public interface CategoryMapper {
	CategoryMapper INSTANCE = Mappers.getMapper(CategoryMapper.class);

	Category categoryDtoToCategory(CategoryDto categoryDto);
}
