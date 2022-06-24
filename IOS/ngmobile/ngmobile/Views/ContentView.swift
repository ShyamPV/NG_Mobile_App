//
//  ContentView.swift
//  ngmobile
//
//  Created by Shyam Vekaria on 16/02/22.
//

import SwiftUI

struct ContentView: View {
    
    @EnvironmentObject var loginViewModel:LoginViewModel
    
    var body: some View {
        NavigationView{
            if loginViewModel.signedIn{
                HomeView()
            } else {
                LoginView()
            }
        }.onAppear {
            loginViewModel.signedIn = loginViewModel.isSignedIn
        }
    }
}

struct LoginView: View{
    
    @State var emailText = ""
    @State var passwordText = ""
    
    @EnvironmentObject var model:LoginViewModel
    
    var body: some View{
        VStack{
            LoginHeader()
            VStack {
                VStack(alignment: .leading){
                    HStack{
                        Spacer()
                        Text("Login")
                            .font(.title)
                        Spacer()
                    }
                    Text("Email").padding(.leading,16)
                    TextField("",text: $emailText).textFieldStyle(.roundedBorder).disableAutocorrection(true)
                        .autocapitalization(.none)
                    Text("Password").padding(.leading,16)
                    SecureField("",text: $passwordText).textFieldStyle(.roundedBorder).disableAutocorrection(true)
                        .autocapitalization(.none)
                    HStack{
                        Spacer()
                        NavigationLink {
                            ResetPasswordForm()
                        } label: {
                            Text("Forgot Password").font(.subheadline).foregroundColor(.red)
                        }
                        
                    }.padding(20)
                    HStack {
                        Spacer()
                        Button{
                            model.login(email: emailText, password: passwordText)
                            emailText = ""
                            passwordText = ""
                        } label:{
                            Text("Login").font(.title2).fontWeight(.bold).frame(width: 200, height: 40).foregroundColor(.white).background(Color.accentColor).cornerRadius(5)
                        }.alert(isPresented: $model.showAlert,content: {
                            Alert(title: Text("Error"), message: Text(model.alertMsg), dismissButton: .destructive(Text("OK"), action: {
                                
                            }))
                        })
                        Spacer()
                    }
                }.padding(20)
                Spacer()
            }
            Spacer()
        }
    }
    
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
