package com.dental.clinic.management.exception.validation;

public class DuplicateTypeCodeException extends RuntimeException {
    public DuplicateTypeCodeException(String typeCode) {
        super("Mã loại '" + typeCode + "' đã tồn tại");
    }
}
