package com.example.mrservice.models;

import java.io.Serializable;

public class UserProfileModel implements Serializable {

    public static final String STRING_RATING_REF = "userRating";
    public static final String STRING_RATING_COUNTS_REF = "ratingCounts";
    public static final String STRING_USER_PROFILE_ID_REF = "userId";
    public static final String STRING_USER_BALANCE_REF = "userBalance";

    String userName, userImageUrl, userEmailAddress, userMobileNumber, userAddress, userAddressLatLng, userType, userServiceCatId, about;
    int ratingCounts;
    long userBalance;
    float userRating;

    public UserProfileModel() {
    }

    public UserProfileModel(String userName, String userImageUrl, String userEmailAddress, String userMobileNumber, String userAddress, String userAddressLatLng, String userType, String userServiceCatId, String about, float userRating, int ratingCounts, long userBalance) {
        this.userName = userName;
        this.userImageUrl = userImageUrl;
        this.userEmailAddress = userEmailAddress;
        this.userMobileNumber = userMobileNumber;
        this.userAddress = userAddress;
        this.userAddressLatLng = userAddressLatLng;
        this.userType = userType;
        this.userServiceCatId = userServiceCatId;
        this.about = about;
        this.userRating = userRating;
        this.ratingCounts = ratingCounts;
        this.userBalance = userBalance;
    }

    public String getAbout() {
        return about;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserImageUrl() {
        return userImageUrl;
    }

    public String getUserEmailAddress() {
        return userEmailAddress;
    }

    public String getUserMobileNumber() {
        return userMobileNumber;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public String getUserAddressLatLng() {
        return userAddressLatLng;
    }

    public String getUserType() {
        return userType;
    }

    public String getUserServiceCatId() {
        return userServiceCatId;
    }

    public float getUserRating() {
        return userRating;
    }

    public int getRatingCounts() {
        return ratingCounts;
    }

    public long getUserBalance() {
        return userBalance;
    }

}
