package com.bank.service;

import com.bank.dto.response.UserReportDto;
import com.bank.entity.User;
import com.bank.mapper.UserReportMapper;
import com.bank.repository.UserRepository;
import com.bank.service.report.ReportGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final UserRepository userRepository;
    private final Map<String, ReportGenerator> reportGenerators;
    private final UserReportMapper userReportMapper;

    public byte[] generateReport(String type) {
        log.info("Generating report of type: {}", type);

        ReportGenerator generator = reportGenerators.get(type.toLowerCase());

        if (generator == null) {
            throw new UnsupportedOperationException(type);
        }

        List<UserReportDto> users = getAllUsersForReport();

        return generator.generate(users);
    }

    public String getContentType(String type) {
        ReportGenerator generator = reportGenerators.get(type.toLowerCase());
        if (generator == null) {
            throw new IllegalArgumentException("Unsupported report type: " + type);
        }
        return generator.getContentType();
    }

    public String getFileExtension(String type) {
        ReportGenerator generator = reportGenerators.get(type.toLowerCase());
        if (generator == null) {
            throw new IllegalArgumentException("Unsupported report type: " + type);
        }
        return generator.getFileExtension();
    }

    private List<UserReportDto> getAllUsersForReport() {
        List<User> users = userRepository.findAll();
        return userReportMapper.toReportDtoList(users);
    }

}