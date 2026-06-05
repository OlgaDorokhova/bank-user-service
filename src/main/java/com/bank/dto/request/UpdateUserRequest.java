package com.bank.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class UpdateUserRequest {
    private List<EmailOperation> emails;
    private List<PhoneOperation> phones;

    @Data
    public static class EmailOperation {
        private String operation; // ADD, DELETE, UPDATE
        private String oldEmail;  // для UPDATE/DELETE
        private String newEmail;  // для ADD/UPDATE
    }

    @Data
    public static class PhoneOperation {
        private String operation; // ADD, DELETE, UPDATE
        private String oldPhone;  // для UPDATE/DELETE
        private String newPhone;  // для ADD/UPDATE
    }
}
