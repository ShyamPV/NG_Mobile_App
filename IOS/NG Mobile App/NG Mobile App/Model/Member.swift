//
//  Member.swift
//  NG Mobile App
//
//  Created by Shyam Vekaria on 7/03/22.
//

import Foundation
import Firebase

struct Member {
    
    init(data: [String:Any]){
        
        self.userID = data["userID"]as? String ?? ""
        self.membershipNo = data["membershipNo"] as? String ?? ""
        self.fullName = data["fullName"] as? String ?? ""
        self.phoneNumber = data["phoneNumber"] as? String ?? ""
        self.email = data["email"] as? String ?? ""
        self.postAddress = data["postAddress"] as? String ?? ""
        self.zipCode = data["zipCode"] as? String ?? ""
        self.city = data["city"] as? String ?? ""
        self.country = data["country"] as? String ?? ""
        self.memberType = data["memberType"] as? String ?? ""
        self.accountStatus = data["accountStatus"] as? String ?? ""
        self.memberExpiryDate = getTime(data: data["memberExpiryDate"] as! Timestamp)
        self.gymExpiryDate = getTime(data: data["gymExpiryDate"] as! Timestamp)
        self.firstTimeLogin = data["firstTimeLogin"] as? Bool ?? true
    }
    
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
    var memberExpiryDate: Date = Date()
    var gymExpiryDate: Date = Date()
    var firstTimeLogin: Bool = true
    
    var stringMemberExpiryDate: String {
        let formatter = DateFormatter()
        formatter.dateFormat = "dd-MM-yyyy"
        return formatter.string(from: memberExpiryDate)
    }
    
    var stringGymExpiryDate:String {
        let formatter = DateFormatter()
        formatter.dateFormat = "dd-MM-yyyy"
        return formatter.string(from: gymExpiryDate)
    }
    
    private func getTime(data: Timestamp) -> Date{
        let date = data.dateValue()
        
        return Calendar.current.date(byAdding: .hour, value: -3, to: date)!
    }
}
