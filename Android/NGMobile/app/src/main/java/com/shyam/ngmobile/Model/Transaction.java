package com.shyam.ngmobile.Model;

import java.util.Date;

public class Transaction {

    private String transactionID;
    private Date transactionDate;
    private String transactionType;
    private Date newExpiryDate;
    private boolean isPending;
    private String memberID;
    private String membershipNo;
    private String memberName;

    public Transaction() {

    }

    public Transaction(String transactionID, Date transactionDate,
                       String transactionType, Date newExpiryDate, boolean isPending,
                       String memberID, String membershipNo, String memberName) {
        this.transactionID = transactionID;
        this.transactionDate = transactionDate;
        this.transactionType = transactionType;
        this.newExpiryDate = newExpiryDate;
        this.isPending = isPending;
        this.memberID = memberID;
        this.membershipNo = membershipNo;
        this.memberName = memberName;
    }

    public String getTransactionID() {
        return transactionID;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public Date getNewExpiryDate() {
        return newExpiryDate;
    }

    public boolean isPending() {
        return isPending;
    }

    public String getMemberID() {
        return memberID;
    }

    public String getMembershipNo() {
        return membershipNo;
    }

    public String getMemberName() {
        return memberName;
    }
}
