package yh.ban.project.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import yh.ban.project.dto.CategoryDto;
import yh.ban.project.model.Category;

@Mapper
public interface CategoryMapper {
	CategoryMapper INSTANCE = Mappers.getMapper(CategoryMapper.class);

	Category categoryDtoToCategory(CategoryDto categoryDto);
}
