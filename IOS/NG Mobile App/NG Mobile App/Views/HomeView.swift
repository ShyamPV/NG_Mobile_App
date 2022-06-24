//
//  HomeView.swift
//  NG Mobile App
//
//  Created by Shyam Vekaria on 7/03/22.
//

import SwiftUI
import Firebase



class HomeViewModel: ObservableObject{
    
    let memberRef = FirebaseManager.shared.firestore.collection("member")
    
    @Published var member:Member?
    @Published var isUserCurrentlyLoggedOut = false
    @Published var isDefaultedMember = false
    @Published var message = ""
    
    
    init(){
        fetchCurrentMember()
    }
    
    
    func fetchCurrentMember(){
        
        guard let userID = FirebaseManager.shared.auth.currentUser?.uid else {
            handleSignOut()
            return}
        
        self.isUserCurrentlyLoggedOut = false
        
        memberRef.document(userID).getDocument { snapshot, error in
            guard error == nil else{
                return
            }
            
            guard let data = snapshot?.data() else{
                return
            }
            
            let member = Member.init(data: data)
            self.validateUser(member: member)
        }
    }
    
    private func validateUser(member: Member){
        
        print(member.accountStatus)
        
        guard member.memberExpiryDate > Date() && member.accountStatus == "Active" else {
            if (member.accountStatus == "Cancelled"){
                message = "This membership account has been cancelled. Please remove this app."
            }else{
                message = "This membership account has defaulted. Please contact the club for further instructions."
            }
            
            DispatchQueue.main.async {
                self.isDefaultedMember = true;
            }
            
            return;
        }
        
        DispatchQueue.main.async {
            self.isUserCurrentlyLoggedOut = false
        }
       
        self.member = member
    }
    
    func getTime(timeStamp:Timestamp) -> Date{
        let date = timeStamp.dateValue()
        
        return Calendar.current.date(byAdding: .hour, value: -3, to: date)!
    }
    
    func handleSignOut(){
        isUserCurrentlyLoggedOut = true;
        try? FirebaseManager.shared.auth.signOut()
    }
}

struct HomeView: View {
    
    @State var showLogoutOption = false;
    @ObservedObject var model = HomeViewModel()
    
    var body: some View {
        VStack{
            //Header
            HStack {
                Text(model.member?.fullName ?? "Nairobi Gymkhana")
                    .font(.system(size: 24, weight: .bold))
                    .foregroundColor(Color("AccentColor"))
                Spacer()
                Button {
                    showLogoutOption.toggle()
                } label: {
                    Image(systemName: "gear")
                        .font(.system(size: 24, weight: .bold))
                        .foregroundColor(Color(.label))
                }
            }.padding()
            
            TabView {
                PostListView()
                    .tabItem {
                        Image(systemName: "house.circle.fill")
                        Text("Club Posts")
                    }
                AccountView()
                    .tabItem {
                        Image(systemName: "person.circle.fill")
                        Text("Account")
                    }
            }
        }.actionSheet(isPresented: $showLogoutOption) {
            .init(title: Text("Sign Out"), message: Text("Are you sure you want to sign out?"),
                  buttons: [
                    .destructive(Text("Sign out"), action: {
                        model.handleSignOut()
                    }),
                    .cancel()])
        }.fullScreenCover(isPresented: $model.isUserCurrentlyLoggedOut, onDismiss: nil) {
            LoginView(didCompleteLoginProcess: {
                self.model.fetchCurrentMember()
            })
        }.alert(isPresented: $model.isDefaultedMember){
            Alert(title: Text("Error"), message: Text(model.message), dismissButton:.destructive(Text("OK")){
                model.handleSignOut()
        })
        }
    }
}

struct HomeView_Previews: PreviewProvider {
    static var previews: some View {
        HomeView()
    }
}
