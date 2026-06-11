package com.bank.service.imports;

import com.bank.dto.request.ImportUserDto;
import com.bank.exception.CustomExceptions;

import java.io.InputStream;
import java.util.List;

public interface ImportParser {

    String getType();  // csv, xlsx и т.д.

    List<ImportUserDto> parse(InputStream inputStream) throws CustomExceptions.ImportException;
}
