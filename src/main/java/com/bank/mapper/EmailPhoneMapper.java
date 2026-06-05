package com.bank.mapper;

import com.bank.config.MapStructConfig;
import com.bank.dto.request.UpdateUserRequest;
import com.bank.entity.EmailData;
import com.bank.entity.PhoneData;
import com.bank.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(config = MapStructConfig.class)
public interface EmailPhoneMapper {

    @Named("toEmailEntities")
    default List<EmailData> toEmailEntities(List<String> emails, User user) {
        return emails.stream()
                .map(email -> EmailData.builder()
                        .email(email)
                        .user(user)
                        .build())
                .collect(Collectors.toList());
    }

    @Named("toPhoneEntities")
    default List<PhoneData> toPhoneEntities(List<String> phones, User user) {
        return phones.stream()
                .map(phone -> PhoneData.builder()
                        .phone(phone)
                        .user(user)
                        .build())
                .collect(Collectors.toList());
    }
}
