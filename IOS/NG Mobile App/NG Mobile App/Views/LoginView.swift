//
//  ContentView.swift
//  NG Mobile App
//
//  Created by Shyam Vekaria on 7/03/22.
//

import SwiftUI
import Firebase

struct LoginView: View {
    
    let didCompleteLoginProcess: () -> ()
    
    @State private var email = ""
    @State private var password = ""
    @State private var message = ""
    
    @State private var isLoginMode = true
    @State private var showMessageBox = false
    @State private var isLoading = false;
    @State private var value = 1.0
    
    var body: some View {
        HStack {
            ScrollView{
                VStack{
                    VStack{
                        Image("ng_logo_transparent")
                            .resizable()
                            .frame(width: 120, height: 150)
                            .padding(.top, 48)
                        Text("Nairobi Gymkhana")
                            .font(.largeTitle)
                            .fontWeight(.semibold)
                            .foregroundColor(.white)
                            .padding(.bottom,20)
                    }.frame(maxWidth: .infinity)
                        .background(Color.accentColor)
                    
                    Picker(selection: $isLoginMode, label: Text("Pick Here")){
                        Text("Log in").tag(true)
                        Text("Reset Password").tag(false)
                    }.pickerStyle(SegmentedPickerStyle())
                        .padding()
                    VStack(alignment: .leading){
                        HStack {
                            Spacer()
                            Text(isLoginMode ? "Log in": "Reset Password").font(.headline)
                            Spacer()
                        }
                        
                        if !isLoginMode {
                            Text("Please enter the email address to send the reset link ").padding(8.0)
                        }
                        
                        Text("Email").padding(.leading,16)
                        TextField("",text: $email)
                            .autocapitalization(.none)
                            .disableAutocorrection(true)
                            .keyboardType(.emailAddress)
                            .textFieldStyle(.roundedBorder)
                        
                        if isLoginMode {
                            Text("Password").padding(.leading,16)
                            SecureField("",text: $password)
                                .autocapitalization(.none)
                                .disableAutocorrection(true)
                                .textFieldStyle(.roundedBorder)
                        }
                        
                        HStack{
                            Spacer()
                            Button {
                                if(!isLoading){
                                    button_clicked()
                                }
                            } label: {
                                if(isLoading){
                                    ProgressView()
                                        .frame(width: 200, height: 40)
                                        .background(Color.accentColor)
                                        .progressViewStyle(CircularProgressViewStyle(tint: Color.white))
                                        .cornerRadius(5)
                                }else{
                                    Text(isLoginMode ? "Log in": "Reset Password")
                                        .fontWeight(.semibold)
                                        .frame(width: 200, height: 40)
                                        .background(Color.accentColor)
                                        .foregroundColor(.white)
                                        .cornerRadius(5)
                                }
                            }
                            Spacer()
                        }.padding(.top,8).actionSheet(isPresented: $showMessageBox){
                            .init(title: Text("Message"),message: Text(message), buttons: [.destructive(Text("OK")){
                                message = ""
                            }])
                        }
                    }.padding(.top,8).padding(8)
                }
            }.ignoresSafeArea(edges: .top)
        }
    }
    
    private func button_clicked(){
        isLoading = true;
        if isLoginMode{
            guard email != "",password != "" else{
                isLoading = false;
                message += "Please enter credentials to login."
                showMessageBox.toggle()
                return
            }
            
            loginUser(email: email, password: password)
        }else{
            guard email != "" else{
                isLoading = false;
                message = "Please enter the email address to send reset link."
                showMessageBox.toggle()
                return
            }
            
            resetPassword(email: email)
        }
    }
    
    
    private func loginUser(email:String,password:String){
        
       
        FirebaseManager.shared.auth.signIn(withEmail: email, password: password) { result, error in
            guard error == nil else{
                isLoading = false;
                message = "Incorrect credentials entered\nPlease try again."
                showMessageBox.toggle()
                return
            }
            isLoading = false;
            self.didCompleteLoginProcess()
        }
    }
    
    private func resetPassword(email:String){
        FirebaseManager.shared.auth.sendPasswordReset(withEmail: email, completion: { error in
            guard error == nil else{
                isLoading = false;
                message = "Please enter the correct email address"
                showMessageBox.toggle()
                return
            }
            
            isLoading = false;
            message = "Email sent successfully"
            showMessageBox.toggle()
        })
    }
}

struct LoginView_Previews: PreviewProvider {
    static var previews: some View {
        LoginView(didCompleteLoginProcess: {            
        })
    }
}

