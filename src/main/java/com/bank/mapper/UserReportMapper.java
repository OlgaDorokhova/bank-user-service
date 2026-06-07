package com.bank.mapper;

import com.bank.config.MapStructConfig;
import com.bank.entity.User;
import com.bank.dto.response.UserReportDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(config = MapStructConfig.class)
public interface UserReportMapper {
    @Mapping(target = "emails", source = "user", qualifiedByName = "mapEmails")
    @Mapping(target = "phones", source = "user", qualifiedByName = "mapPhones")
    @Mapping(target = "balance", source = "user.account.balance")
    @Mapping(target = "initialBalance", source = "user.account.initialBalance")
    UserReportDto toReportDto(User user);

    List<UserReportDto> toReportDtoList(List<User> users);

    @Named("mapEmails")
    default String mapEmails(User user) {
        return user.getEmails() == null ? "" :
                user.getEmails().stream()
                        .map(e -> e.getEmail())
                        .collect(Collectors.joining(";"));
    }

    @Named("mapPhones")
    default String mapPhones(User user) {
        return user.getPhones() == null ? "" :
                user.getPhones().stream()
                        .map(p -> p.getPhone())
                        .collect(Collectors.joining(";"));
    }
}
