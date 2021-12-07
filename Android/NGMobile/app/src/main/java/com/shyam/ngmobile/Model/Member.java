package com.shyam.ngmobile.Model;

import com.shyam.ngmobile.Enums.MemberStatus;

import java.util.Date;

public class Member {
    private String userID;
    private String membershipNo;
    private String fullName;
    private String phoneNumber;
    private String email;
    private String postAddress;
    private String zipCode;
    private String city;
    private String country;
    private String memberType;
    private MemberStatus accountStatus;
    private Date memberExpiryDate;
    private Date gymExpiryDate;
    private boolean firstTimeLogin;

    public Member() {
    }

    public Member(String userID, String membershipNo, String fullName, String phoneNumber,
                  String email, String postAddress, String zipCode, String city, String country,
                  String memberType, MemberStatus accountStatus, Date memberExpiryDate, Date gymExpiryDate, boolean firstTimeLogin) {
        this.userID = userID;
        this.membershipNo = membershipNo;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.postAddress = postAddress;
        this.zipCode = zipCode;
        this.city = city;
        this.country = country;
        this.memberType = memberType;
        this.accountStatus = accountStatus;
        this.memberExpiryDate = memberExpiryDate;
        this.gymExpiryDate = gymExpiryDate;
        this.firstTimeLogin = firstTimeLogin;
    }

    public Member(String userID, String membershipNo, String fullName, String phoneNumber,
                  String email, String memberType, MemberStatus accountStatus,
                  Date memberExpiryDate, Date gymExpiryDate, boolean firstTimeLogin) {
        this.userID = userID;
        this.membershipNo = membershipNo;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.memberType = memberType;
        this.accountStatus = accountStatus;
        this.memberExpiryDate = memberExpiryDate;
        this.gymExpiryDate = gymExpiryDate;
        this.firstTimeLogin = firstTimeLogin;
    }


    public String getUserID() {
        return userID;
    }

    public String getMembershipNo() {
        return membershipNo;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getPostAddress() {
        return postAddress;
    }

    public String getZipCode() {
        return zipCode;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public String getMemberType() {
        return memberType;
    }

    public MemberStatus getAccountStatus() {
        return accountStatus;
    }

    public Date getMemberExpiryDate() {
        return memberExpiryDate;
    }

    public Date getGymExpiryDate() {
        return gymExpiryDate;
    }

    public boolean isFirstTimeLogin() {
        return firstTimeLogin;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setPostAddress(String postAddress) {
        this.postAddress = postAddress;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setMemberExpiryDate(Date memberExpiryDate) {
        this.memberExpiryDate = memberExpiryDate;
    }

    public void setGymExpiryDate(Date gymExpiryDate) {
        this.gymExpiryDate = gymExpiryDate;
    }

    public void setFirstTimeLogin(boolean firstTimeLogin) {
        this.firstTimeLogin = firstTimeLogin;
    }


}
