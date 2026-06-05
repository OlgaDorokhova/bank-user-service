package com.bank.mapper;

import com.bank.config.MapStructConfig;
import com.bank.dto.response.UserResponse;
import com.bank.entity.User;
import com.bank.entity.EmailData;
import com.bank.entity.PhoneData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(config = MapStructConfig.class)
public interface UserMapper {

    @Mapping(target = "emails", source = "user", qualifiedByName = "mapEmails")
    @Mapping(target = "phones", source = "user", qualifiedByName = "mapPhones")
    @Mapping(target = "balance", source = "user", qualifiedByName = "mapBalance")
    UserResponse toResponse(User user);

    @Named("mapEmails")
    default List<String> mapEmails(User user) {
        return user.getEmails().stream()
                .map(EmailData::getEmail)
                .collect(Collectors.toList());
    }

    @Named("mapPhones")
    default List<String> mapPhones(User user) {
        return user.getPhones().stream()
                .map(PhoneData::getPhone)
                .collect(Collectors.toList());
    }

    @Named("mapBalance")
    default BigDecimal mapBalance(User user) {
        return user.getAccount() != null ? user.getAccount().getBalance() : BigDecimal.ZERO;
    }
}