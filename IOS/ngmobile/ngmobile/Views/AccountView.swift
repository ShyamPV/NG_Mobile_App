//
//  AccountView.swift
//  ngmobile
//
//  Created by Shyam Vekaria on 16/02/22.
//

import SwiftUI

struct AccountView: View {
    
    @State var postAddress:String = ""
    @State var city:String = ""
    @State var country:String = ""
    @State var zipCode:String = ""
    @State var phoneNumber:String = ""
    
    @State var membershipNo:String = ""
    @State var membershipType:String = ""
    @State var memebrExpiryDate:String = ""
    
    @State var email:String = ""
    @State var password:String = ""
    @State var confirmPassword:String = ""
    
    var body: some View {
        ScrollView{
            VStack(alignment: .leading){
                
                VStack(alignment: .leading){
                    Text("Address")
                        .font(.title).padding()
                    VStack(alignment: .leading){
                        Text("Post Address").padding(.leading,16)
                        TextField("Post Address", text: $postAddress).textFieldStyle(.roundedBorder)
                    }
                    VStack(alignment: .leading){
                        Text("City").padding(.leading,16)
                        TextField("City", text: $city).textFieldStyle(.roundedBorder)
                    }
                    VStack(alignment: .leading){
                        Text("Country").padding(.leading,16)
                        TextField("Country", text: $country).textFieldStyle(.roundedBorder)
                    }
                    VStack(alignment: .leading){
                        Text("Zip Code").padding(.leading,16)
                        TextField("Zip Code", text: $zipCode).textFieldStyle(.roundedBorder)
                    }
                    VStack(alignment: .leading){
                        Text("Phone Number").padding(.leading,16)
                        TextField("Phone Number", text: $phoneNumber).textFieldStyle(.roundedBorder)
                    }
                }.padding()
                
                VStack(alignment: .leading){
                    Text("Membership")
                        .font(.title).padding()
                    
                    VStack(alignment: .leading){
                        Text("Memberhip Number").padding(.leading,16)
                        TextField("Memberhip Number",text: $membershipNo).textFieldStyle(.roundedBorder).disabled(true)
                    }
                    VStack(alignment: .leading){
                        Text("Memberhip Type").padding(.leading,16)
                        TextField("",text: $membershipType).textFieldStyle(.roundedBorder).disabled(true)
                    }
                    VStack(alignment: .leading){
                        Text("Memberhip Expiry Date").padding(.leading,16)
                        TextField("",text: $memebrExpiryDate).textFieldStyle(.roundedBorder).disabled(true)
                    }
                    
                    
                }.padding()
                
                VStack(alignment: .leading){
                    
                }
                
                VStack(alignment: .leading){
                    Text("Credentials")
                        .font(.title).padding()
                    Text("Email").padding(.leading,16)
                    TextField("Email", text: $email).textFieldStyle(.roundedBorder).disabled(true)
                    Text("Password").padding(.leading,16)
                    SecureField("Password", text: $password).textFieldStyle(.roundedBorder)
                    Text("Confirm Password").padding(.leading,16)
                    SecureField("Confrim Password", text: $confirmPassword).textFieldStyle(.roundedBorder)
                }.padding()
            }
        }
    }
}

struct AccountView_Previews: PreviewProvider {
    static var previews: some View {
        AccountView()
    }
}
