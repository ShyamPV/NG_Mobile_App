//
//  FirebaseManager.swift
//  NG Mobile App
//
//  Created by Shyam Vekaria on 8/03/22.
//

import Foundation
import Firebase

class FirebaseManager:NSObject{
    
    let auth: Auth
    let firestore: Firestore
    let storage: Storage
    
    static let shared = FirebaseManager()
    
    override init(){
        FirebaseApp.configure()
        
        self.auth = Auth.auth()
        self.firestore = Firestore.firestore()
        self.storage = Storage.storage()
        
        super.init()
    }
}
