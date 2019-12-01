package com.example.mrservice.models;

import java.io.Serializable;

public class TaskBid implements Serializable {

    String bidBuyerId, bidMessage;

    public TaskBid() {
    }

    public TaskBid(String bidBuyerId, String bidMessage) {
        this.bidBuyerId = bidBuyerId;
        this.bidMessage = bidMessage;
    }

    public String getBidBuyerId() {
        return bidBuyerId;
    }

    public String getBidMessage() {
        return bidMessage;
    }
}
