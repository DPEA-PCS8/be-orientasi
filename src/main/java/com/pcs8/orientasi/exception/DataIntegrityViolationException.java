package com.pcs8.orientasi.exception;

public class DataIntegrityViolationException extends RuntimeException {

    private final String relatedEntity;
    private final int count;

    public DataIntegrityViolationException(String message) {
        super(message);
        this.relatedEntity = null;
        this.count = 0;
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
