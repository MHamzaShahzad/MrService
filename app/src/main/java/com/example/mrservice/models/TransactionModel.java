package com.example.mrservice.models;

public class TransactionModel {

    public static final String SUBMITTED_AT_DATE_TIME_REF = "submitAtDateTime";
    public static final String TRANSACTION_STATUS_REF = "transactionStatus";

    private String taskId, totalAmount, deductedFrom, creditedTo, deductedAtDateTime, submitAtDateTime, transactionStatus;

    public TransactionModel() {
    }

    public TransactionModel(String taskId, String totalAmount, String deductedFrom, String creditedTo, String deductedAtDateTime, String submitAtDateTime, String transactionStatus) {
        this.taskId = taskId;
        this.totalAmount = totalAmount;
        this.deductedFrom = deductedFrom;
        this.creditedTo = creditedTo;
        this.deductedAtDateTime = deductedAtDateTime;
        this.submitAtDateTime = submitAtDateTime;
        this.transactionStatus = transactionStatus;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public String getDeductedFrom() {
        return deductedFrom;
    }

    public String getCreditedTo() {
        return creditedTo;
    }

    public String getDeductedAtDateTime() {
        return deductedAtDateTime;
    }

    public String getSubmitAtDateTime() {
        return submitAtDateTime;
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }

}
