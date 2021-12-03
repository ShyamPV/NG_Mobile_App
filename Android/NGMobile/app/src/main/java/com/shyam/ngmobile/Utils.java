package com.shyam.ngmobile;


import com.shyam.ngmobile.Model.Member;

public class Utils {
    static Member currentMember;

    public static void setCurrentMember(Member member) {
        currentMember = member;
    }

    public static Member getCurrentMember() {
        if (currentMember != null)
            return currentMember;
        throw new NullPointerException("No Logged in Member");
    }
}
