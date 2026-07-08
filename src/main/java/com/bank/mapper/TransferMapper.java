package com.bank.mapper;

import com.bank.config.MapStructConfig;
import com.bank.dto.response.TransferResponse;
import com.bank.entity.Transfer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface TransferMapper {

    @Mapping(target = "fromUserId", source = "fromAccount.user.id")
    @Mapping(target = "toUserId", source = "toAccount.user.id")
    @Mapping(target = "status", source = "status")  // enum → string автоматически
    TransferResponse toResponse(Transfer transfer);
}
