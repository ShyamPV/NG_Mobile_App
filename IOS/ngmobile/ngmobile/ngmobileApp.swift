//
//  ngmobileApp.swift
//  ngmobile
//
//  Created by Shyam Vekaria on 16/02/22.
//

import SwiftUI
import Firebase

@main
struct ngmobileApp: App {
        
    init(){
        FirebaseApp.configure()
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView().environmentObject(LoginViewModel())
        }
    }
}
