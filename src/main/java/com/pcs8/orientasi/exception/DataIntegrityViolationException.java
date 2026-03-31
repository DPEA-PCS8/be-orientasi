package com.pcs8.orientasi.exception;

public class DataIntegrityViolationException extends RuntimeException {

    private String relatedEntity;
    private int count;

    public DataIntegrityViolationException(String message) {
        super(message);
    }

    public DataIntegrityViolationException(String message, String relatedEntity, int count) {
        super(message);
        this.relatedEntity = relatedEntity;
        this.count = count;
    }

    public String getRelatedEntity() {
        return relatedEntity;
    }

    public int getCount() {
        return count;
    }
}
