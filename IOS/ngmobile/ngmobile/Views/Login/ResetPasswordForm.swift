//
//  ResetPasswordForm.swift
//  ngmobile
//
//  Created by Shyam Vekaria on 28/02/22.
//

import SwiftUI

struct ResetPasswordForm: View {
    
    @ObservedObject var model = LoginViewModel()

    @State var emailText = ""
    
    var body: some View {
        VStack(alignment: .leading){
            HStack{
                Spacer()
                Text("Reset Password")
                    .font(.title)
                Spacer()
            }.padding(8)
            HStack{
                Text("Please type the email address to send the reset link.")
                    .font(.body)
            }.padding(8)
            
            Text("Email").padding(.leading,16)
            TextField("",text: $emailText).textFieldStyle(.roundedBorder)
                .disableAutocorrection(true)
                .autocapitalization(.none)
            HStack{
                Spacer()
                Button {
                    model.resetPassword(email: emailText)
                } label: {
                    Text("Send Link").font(.title2).fontWeight(.bold).frame(width: 200, height: 40).foregroundColor(.white).background(Color.accentColor).cornerRadius(5)
                }.alert(isPresented: $model.showAlert,content: {
                    Alert(title: Text("Error"), message: Text(model.alertMsg), dismissButton: .destructive(Text("OK"), action: {
                        
                    }))
                }).padding(20)

                Spacer()
            }
            
        }.padding(16)
        Spacer()
    }
}

struct ResetPasswordForm_Previews: PreviewProvider {
    static var previews: some View {
        ResetPasswordForm()
    }
}
