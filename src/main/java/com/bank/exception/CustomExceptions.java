package com.bank.exception;

public class CustomExceptions {

    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }

    public static class InsufficientFundsException extends RuntimeException {
        public InsufficientFundsException(String message) {
            super(message);
        }
    }

    public static class SameUserTransferException extends RuntimeException {
        public SameUserTransferException(String message) {
            super(message);
        }
    }

    public static class EmailAlreadyExistsException extends RuntimeException {
        public EmailAlreadyExistsException(String message) {
            super(message);
        }
    }

    public static class PhoneAlreadyExistsException extends RuntimeException {
        public PhoneAlreadyExistsException(String message) {
            super(message);
        }
    }

    public static class EmailNotFoundException extends RuntimeException {
        public EmailNotFoundException(String message) {
            super(message);
        }
    }

    public static class PhoneNotFoundException extends RuntimeException {
        public PhoneNotFoundException(String message) {
            super(message);
        }
    }

    public static class UnsupportedReportTypeException extends RuntimeException {
        public UnsupportedReportTypeException(String type) {
            super("Unsupported report type: " + type + ". Supported: csv, pdf");
        }
    }

    public static class ImportException extends RuntimeException {
        public ImportException(String message) {
            super(message);
        }
    }
}
