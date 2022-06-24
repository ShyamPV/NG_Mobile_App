//
//  LoginViewModel.swift
//  ngmobile
//
//  Created by Shyam Vekaria on 28/02/22.
//

import Foundation
import Firebase
import SwiftUI

class LoginViewModel: ObservableObject{
    
    init(){
        if isSignedIn {
            getCurrentUser();
        }
    }
    
    let mAuth = Auth.auth()
    let memberRef = Firestore.firestore().collection("member")
    @Published var member:Member?
    @Published var signedIn = false
    
    var isSignedIn: Bool{
        return mAuth.currentUser != nil
    }
    
    
    @Published var alertMsg:String = ""
    @Published var showAlert:Bool = false
    
    
    func login(email:String, password:String){
        mAuth.signIn(withEmail: email, password: password) { result, error in
            if error == nil{
                self.getCurrentUser()
            } else {
                self.reportError(error: "Incorrect Credentials")
            }
        }
    }
    
    
    func getCurrentUser(){
        
        let userID = self.mAuth.currentUser!.uid;
        
        guard userID != "" else{
            self.reportError(error: "User Not Found")
            self.logoutUser()
            return
        }
        
        self.memberRef.document(userID).getDocument { snapshot, error in
            guard error == nil else{
                self.reportError(error: "User Not Found")
                return
            }
            
            if let snapshot = snapshot {
                DispatchQueue.main.async {
                    let memberhipExpiryDate = snapshot["memberExpiryDate"] as! Timestamp
                    let gymExpiryDate = snapshot["gymExpiryDate"] as! Timestamp
                    
                    let member = Member(userID: snapshot["userID"]as? String ?? "",
                                        membershipNo: snapshot["membershipNo"] as? String ?? "",
                                        fullName: snapshot["fullname"] as? String ?? "",
                                        phoneNumber: snapshot["phoneNumber"] as? String ?? "",
                                        email: snapshot["email"] as? String ?? "",
                                        postAddress: snapshot["postAddress"] as? String ?? "",
                                        zipCode: snapshot["zipCode"] as? String ?? "",
                                        city: snapshot["city"] as? String ?? "",
                                        country: snapshot["country"] as? String ?? "",
                                        memberType: snapshot["memberType"] as? String ?? "",
                                        accountStatus: snapshot["accountStatus"] as? String ?? "",
                                        memberExpiryDate: self.getTime(timeStamp: memberhipExpiryDate) ,
                                        gymExpiryDate: self.getTime(timeStamp: gymExpiryDate),
                                        firstTimeLogin: snapshot["firstTimeLogin"] as? Bool ?? true)
                    
                    self.validateMember(member: member)
                }
            }else{
                self.reportError(error: "User Not Found")
                return
            }
                
        }
    }
    
    func validateMember(member: Member){
        if(member.memberExpiryDate > Date()){
            self.member = member
            self.signedIn = true;
        }else{
            logoutUser()
            self.reportError(error: "Please pay your subs.")
        }
    }
    
    func logoutUser(){
        try! mAuth.signOut()
        self.signedIn = false;
    }
    
    func resetPassword(email:String){
        mAuth.sendPasswordReset(withEmail: email) { error in
            if error == nil{
                self.reportError(error: "Reset Link Sent Successfully.")
            }else{
                self.reportError(error: "Error! Reset Link not sent")
            }
        }
    }
    
    func reportError(error: String){
        DispatchQueue.main.async {
            self.alertMsg = error
            self.showAlert.toggle()
        }
    }
    
    func getTime(timeStamp:Timestamp) -> Date{
        let date = timeStamp.dateValue()
        
        return Calendar.current.date(byAdding: .hour, value: -3, to: date)!
    }
}
