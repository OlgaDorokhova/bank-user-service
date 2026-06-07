package com.bank.service.report;

import com.bank.dto.response.UserReportDto;

import java.util.List;

public interface ReportGenerator {

    String getType();

    byte[] generate(List<UserReportDto> users);

    String getContentType();

    String getFileExtension();
}
