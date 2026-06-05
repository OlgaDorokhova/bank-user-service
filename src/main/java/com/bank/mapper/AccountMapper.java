package com.bank.mapper;

import com.bank.config.MapStructConfig;
import com.bank.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.math.BigDecimal;

@Mapper(config = MapStructConfig.class)
public interface AccountMapper {

    @Mapping(target = "user", ignore = true)
    Account toEntity(Long userId, BigDecimal initialBalance);
}
