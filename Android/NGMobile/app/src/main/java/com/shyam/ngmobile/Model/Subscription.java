package com.shyam.ngmobile.Model;

public class Subscription {
    private String subID;
    private String memberType;
    private int subsYear;
    private double subsAmount;
    private double sportsLevy;
    private int sportsLevyVAT;
    private double subsTotal;

    public Subscription() {
    }

    public Subscription(String subID, String memberType, int subsYear, double subsAmount,
                        double sportsLevy, int sportsLevyVAT, double subsTotal) {
        this.subID = subID;
        this.memberType = memberType;
        this.subsYear = subsYear;
        this.subsAmount = subsAmount;
        this.sportsLevy = sportsLevy;
        this.sportsLevyVAT = sportsLevyVAT;
        this.subsTotal = subsTotal;
    }

    public String getSubID() {
        return subID;
    }

    public String getMemberType() {
        return memberType;
    }

    public int getSubsYear() {
        return subsYear;
    }

    public double getSubsAmount() {
        return subsAmount;
    }

    public double getSportsLevy() {
        return sportsLevy;
    }

    public int getSportsLevyVAT() {
        return sportsLevyVAT;
    }

    public double getSubsTotal() {
        return subsTotal;
    }



}
