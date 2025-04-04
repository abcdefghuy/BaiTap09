package com.example.imageuploads.model;

import java.util.List;

public class MessageResponse {
    private String message;
    private boolean success;
    private List<ImageUpload> result;

    public MessageResponse(String message, boolean success, List<ImageUpload> data) {
        this.message = message;
        this.success = success;
        this.result = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<ImageUpload> getData() {
        return result;
    }

    public void setData(List<ImageUpload> data) {
        this.result = data;
    }
}
