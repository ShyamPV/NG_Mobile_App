//
//  Member.swift
//  ngmobile
//
//  Created by Shyam Vekaria on 16/02/22.
//

import Foundation

struct Member{
    
    var userID: String
    var membershipNo: String
    var fullName: String
    var phoneNumber: String
    var email: String
    var postAddress: String
    var zipCode: String
    var city: String
    var country: String
    var memberType: String
    var accountStatus: String
    var memberExpiryDate: Date
    var gymExpiryDate: Date
    var firstTimeLogin: Bool
    
    func getMemberExpiryDate() -> String{
        memberExpiryDate.getFormattedDate(format: "dd/MM/yyyy")
    }
}

