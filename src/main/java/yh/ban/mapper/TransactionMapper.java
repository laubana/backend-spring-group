package yh.ban.mapper;

import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import yh.ban.dto.TransactionDto;
import yh.ban.helper.StringHelper;
import yh.ban.model.Transaction;

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
