package yh.ban.project.mapper;

import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import yh.ban.project.dto.TransactionDto;
import yh.ban.project.helper.StringHelper;
import yh.ban.project.model.Transaction;

@Mapper
public interface TransactionMapper {
	TransactionMapper INSTANCE = Mappers.getMapper(TransactionMapper.class);

	@Mapping(source = "userId", target = "user", qualifiedByName = "userIdToUser")
	Transaction transactionDtoToTransaction(TransactionDto commentDto);

	@Named("userIdToUser")
	default ObjectId userIdToUser(String userId) {
		if (StringHelper.isNullOrBlank(userId)) {
			return null;
		}

		ObjectId user = new ObjectId(userId);

		return user;
	}
}
