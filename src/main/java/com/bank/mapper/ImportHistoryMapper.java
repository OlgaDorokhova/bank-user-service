package com.bank.mapper;

import com.bank.config.MapStructConfig;
import com.bank.dto.response.ImportHistoryResponse;
import com.bank.entity.ImportHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface ImportHistoryMapper {

    @Mapping(target = "status", source = "status")  // enum → string автоматически
    ImportHistoryResponse toResponse(ImportHistory history);
}