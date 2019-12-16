package com.example.mrservice.models;

public class ChatModel {

    String senderId, message, dateTimeSent;

    public ChatModel() {
    }

    public ChatModel(String senderId, String message, String dateTimeSent) {
        this.senderId = senderId;
        this.message = message;
        this.dateTimeSent = dateTimeSent;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getMessage() {
        return message;
    }

    public String getDateTimeSent() {
        return dateTimeSent;
    }
}
