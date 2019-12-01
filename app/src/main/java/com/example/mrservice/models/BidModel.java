package com.example.mrservice.models;

import java.io.Serializable;

public class BidModel implements Serializable {

    String bidBuyerId, bidMessage;

    public BidModel() {
    }

    public BidModel(String bidBuyerId, String bidMessage) {
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
