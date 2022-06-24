//
//  AccountView.swift
//  NG Mobile App
//
//  Created by Shyam Vekaria on 10/03/22.
//

import SwiftUI
import PDFKit

class AccountViewModel: NSObject, ObservableObject, UIDocumentInteractionControllerDelegate {
    
    let memberRef = FirebaseManager.shared.firestore.collection("member")
    @Published var member:Member?
    @Published var postAddress:String = ""
    @Published var city:String = ""
    @Published var country:String = ""
    @Published var zipCode:String = ""
    @Published var phoneNumber:String = ""
    
    @Published var membershipNo:String = ""
    @Published var membershipType:String = ""
    @Published var memebrExpiryDate:String = ""
    
    @Published var email:String = ""
    
    @Published var isCertificateUploaded = false
    @Published var certificateURL:URL?
    
    override init(){
        super.init()
        fetchCurrentMember()
    }
    
    
    func fetchCurrentMember(){
        
        guard let userID = FirebaseManager.shared.auth.currentUser?.uid else {
            return}
        
        
        memberRef.document(userID).getDocument { snapshot, error in
            guard error == nil else{
                return
            }
            
            guard let data = snapshot?.data() else{
                return
            }
            DispatchQueue.main.async {
                self.member = Member.init(data: data)
                self.showMemberDetails()
                
                self.getVaccineCertificate(membershipNo: self.member?.membershipNo ?? "")
            }
        }
    }
    
    func showMemberDetails(){
        postAddress = member?.postAddress ?? ""
        city = member?.city ?? ""
        country = member?.country ?? ""
        zipCode =  member?.zipCode ?? ""
        phoneNumber = member?.phoneNumber ?? ""
        
        membershipNo = member?.membershipNo ?? ""
        membershipType =  member?.memberType ?? ""
        memebrExpiryDate =  member?.stringMemberExpiryDate ?? ""
        
        email = member?.email ?? ""
    }
    
    func getVaccineCertificate(membershipNo:String){
        
        let certificateRef = FirebaseManager.shared.storage.reference(withPath: "certificate")
        
        guard membershipNo != "" else{
            self.isCertificateUploaded = false
            return
        }
        
        certificateRef.child(membershipNo).downloadURL { url, error in
            
            guard error == nil else {
                self.isCertificateUploaded = false
                return
            }
            
            self.isCertificateUploaded = true
            self.certificateURL = url
            
        }
    }
    
    func updateCurrentUser(){
        
        let userID = member?.userID ?? ""
        
        guard userID != "" else{
            return
        }
        let updateMember: [String: String] = getUpdateData()
        
        guard updateMember.count > 0 else {
            print("Nothing to udpdate")
            return
        }
        
        memberRef.document(userID).updateData(updateMember) { error in
            guard error == nil else {
                return}
            
            print("Details Updated Successfully")
        }
    }
    
    
    
    func getUpdateData() -> [String: String]{
        var updateData: [String: String] = [:]
        
        if member?.postAddress != self.postAddress{
            updateData["postAddress"] = self.postAddress
        }
        
        if member?.city != self.city{
            updateData["city"] = self.city
        }
        
        if member?.zipCode != self.zipCode{
            updateData["zipCode"] = self.zipCode
        }
        
        if member?.phoneNumber != self.phoneNumber{
            updateData["phoneNumber"] = self.phoneNumber
        }
        
        return updateData
    }
    
    func updatePassword(password: String){
        guard let loggedInMember = FirebaseManager.shared.auth.currentUser else {
            print("No User Found")
            return
        }
        
        loggedInMember.updatePassword(to: password) { error in
            guard error == nil else{
                //Ask the user to login again before updating the password
                print("Password not updated")
                return
            }
            
            print("Password updated")
        }
    }
}

struct AccountView: View {
    
    @ObservedObject var model = AccountViewModel()
    
    @State var password = ""
    @State var confirmPassword = ""
    
    @State var presentImporter = false
    @State var presentCerficate = false
    
    @State var isloading = false
    @State var showAlert = false
    @State var alertMessage = ""
    
    var body: some View {
        NavigationView{
            ScrollView{
                VStack(alignment: .leading){
                    VStack(alignment: .leading){
                        Text("Address")
                            .font(.title).padding()
                        VStack(alignment: .leading){
                            Text("Post Address").padding(.leading,16)
                            TextField(model.member?.postAddress ?? "Update Post Address", text: $model.postAddress).keyboardType(.numberPad).textFieldStyle(.roundedBorder)
                        }
                        VStack(alignment: .leading){
                            Text("City").padding(.leading,16)
                            TextField(model.member?.city ?? "Update City", text: $model.city).textFieldStyle(.roundedBorder)
                        }
                        VStack(alignment: .leading){
                            Text("Country").padding(.leading,16)
                            TextField(model.member?.country ?? "Update Country", text: $model.country).textFieldStyle(.roundedBorder)
                        }
                        VStack(alignment: .leading){
                            Text("Zip Code").padding(.leading,16)
                            TextField(model.member?.zipCode ?? "Update Zip Code", text: $model.zipCode).keyboardType(.numberPad).textFieldStyle(.roundedBorder)
                        }
                        VStack(alignment: .leading){
                            Text("Phone Number").padding(.leading,16)
                            TextField(model.member?.phoneNumber ?? "Update Phone Number", text: $model.phoneNumber).keyboardType(.phonePad).textFieldStyle(.roundedBorder)
                        }
                        
                    }.padding([.top, .leading, .trailing], 8.0)
                    Divider()
                    VStack(alignment: .leading){
                        Text("Membership")
                            .font(.title).padding()
                        VStack(alignment: .leading){
                            Text("Memberhip Number").padding(.leading,16)
                            TextField(model.member?.membershipNo ?? "",text: $model.membershipNo).textFieldStyle(.roundedBorder).disabled(true)
                        }
                        VStack(alignment: .leading){
                            Text("Memberhip Type").padding(.leading,16)
                            TextField( model.member?.memberType ?? "",text: $model.membershipType).textFieldStyle(.roundedBorder).disabled(true)
                        }
                        VStack(alignment: .leading){
                            Text("Memberhip Expiry Date").padding(.leading,16)
                            TextField(model.member?.stringMemberExpiryDate ?? "",text: $model.memebrExpiryDate).textFieldStyle(.roundedBorder).disabled(true)
                        }
//                        HStack {
//                            Spacer()
//
//                            NavigationLink {
//                                StatementView(member: model.member)
//                            } label: {
//                                Text("View Statement")
//                                    .fontWeight(.semibold)
//                                    .frame(width: 200, height: 40)
//                                    .background(Color.accentColor)
//                                    .foregroundColor(.white)
//                                    .cornerRadius(5)
//                            }
//                            Spacer()
//                        }.padding(.vertical,8)
                    }.padding([.top, .leading, .trailing], 8.0)
                    Divider()
                    VStack(alignment: .leading){
                        Text("Vaccine Certificate")
                            .font(.title).padding()
                        HStack {
                            Spacer()
                                
                            if model.isCertificateUploaded {
                                NavigationLink {
                                    PDFDocViewer(url: model.certificateURL,membershipNo: model.membershipNo).onDisappear {
                                        model.getVaccineCertificate(membershipNo: model.membershipNo)
                                    }
                                } label: {
                                    Text("View Certificate")
                                        .fontWeight(.semibold)
                                        .frame(width: 250, height: 40)
                                        .background(Color.accentColor)
                                        .foregroundColor(.white)
                                        .cornerRadius(5)
                                }
                            }else{
                                Button {
                                    presentImporter.toggle()
                                } label: {
                                    Text("Add Certificate")
                                        .fontWeight(.semibold)
                                        .frame(width: 250, height: 40)
                                        .background(Color.accentColor)
                                        .foregroundColor(.white)
                                        .cornerRadius(5)
                                }.fileImporter(isPresented: $presentImporter, allowedContentTypes: [.pdf]) { result in
                                    switch result {
                                    case .success(let url):
                                        uploadCertificate(url:url)
                                        print(url)
                                        
                                    case .failure(let error):
                                        print(error)
                                    }
                                }
                            }
                            
                            Spacer()
                        }.padding(.vertical,8)
                        
                    }
                    Divider()
                    VStack(alignment: .leading){
                        Text("Credentials")
                            .font(.title).padding()
                        Text("Email").padding(.leading,16)
                        TextField( model.member?.email ?? "Email", text: $model.email).textFieldStyle(.roundedBorder).disabled(true)
                        Text("Password").padding(.leading,16)
                        SecureField("Password", text: $password).textFieldStyle(.roundedBorder)
                        Text("Confirm Password").padding(.leading,16)
                        SecureField("Confrim Password", text: $confirmPassword).textFieldStyle(.roundedBorder)
                    }.padding([.top, .leading, .trailing], 8.0)
                    Divider()
                    HStack {
                        Spacer()
                        Button {
                            if(!isloading){
                                updateDetails()
                            }
                        } label: {
                            if(isloading){
                                ProgressView()
                                    .frame(width: 250, height: 40)
                                    .background(Color.accentColor)
                                    .progressViewStyle(CircularProgressViewStyle(tint: Color.white))
                                    .cornerRadius(5)
                            }else{
                                Text("Update Details")
                                    .fontWeight(.semibold)
                                    .frame(width: 250, height: 40)
                                    .background(Color.accentColor)
                                    .foregroundColor(.white)
                                    .cornerRadius(5)
                            }
                        }
                        Spacer()
                    }.padding(.vertical,8)
                }
            }.navigationBarHidden(true).alert(isPresented: $showAlert){
                Alert(title: Text("Message"), message: Text(alertMessage), dismissButton:.destructive(Text("OK")){
            })
            }
        }
    }
    
    
    func validateEntry() -> String {
        var errormessage = ""
        
        if model.postAddress.count < 2 || model.postAddress.count > 5{
            errormessage += "Post Address must be 2-5 digits long. \n"
        }
        
        if model.city == "" {
            errormessage += "City has not been entered.\n"
        }
        
        if model.zipCode.count != 5 {
            errormessage += "Zip code must be 5 digits long.\n"
        }
        
        if model.phoneNumber.count != 10 {
            errormessage += "Phone number must be 10 digits.\n"
        }
        
        return errormessage
    }
    
    private func updateDetails(){
        isloading = true
        
        let message = validateEntry()
        guard message == "" else {
            isloading = false;
            showAlert = true
            alertMessage = message;
            return
        }
        
        model.updateCurrentUser()
        
        guard password != "" else {
            isloading = false
            showAlert = true
            alertMessage = "Update Complete"
            return
        }
        
        guard password == confirmPassword else{
            isloading = false
            showAlert = true
            alertMessage = "Passwords did not match."
            return
        }
        
        updatePassword(password: password)
        
        isloading = false
        showAlert = true
        alertMessage = "Update Complete"
    }
    
    
    private func updatePassword(password:String){
        model.updatePassword(password: password)
        self.password = ""
        self.confirmPassword = ""
    }
    
    
    private func uploadCertificate(url: URL){
        let certificateRef = FirebaseManager.shared.storage.reference(withPath: "certificate")
        
        guard let membershipNo = model.member?.membershipNo else{
            //current user not found
            return
        }
        
        certificateRef.child(membershipNo).putFile(from: url, metadata: nil) { metadata, error in
            guard error == nil else{
                // upload was unsucessful
                print("Certificate not Uploaded")
                return
            }
            
            print("Certificate Uploaded")
            
            model.getVaccineCertificate(membershipNo: membershipNo)
        }
    }
}

struct AccountView_Previews: PreviewProvider {
    
    static var previews: some View {
        AccountView()
    }
}
