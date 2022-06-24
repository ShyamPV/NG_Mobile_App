//
//  PostListView.swift
//  NG Mobile App
//
//  Created by Shyam Vekaria on 10/03/22.
//

import UIKit
import Foundation
import Firebase
import SwiftUI

struct PostListView: View {
    
    private let postRef = FirebaseManager.shared.firestore.collection("post")
    
    @State private var postList: [Post] = []
    @State var alertMsg:String = ""
    @State var showAlert:Bool = false
    
    var body: some View {
        VStack{
            NavigationView{
                List(postList){post in
                    NavigationLink{
                        PostDetail(post: post)
                    } label: {
                        PostRow(post: post)
                    }
                }.refreshable{
                    getAllPosts()
                }
                .onAppear(perform: getAllPosts)
                .navigationBarTitle(Text("Club Updates"), displayMode: .inline)
                Spacer()
            }.alert(isPresented: $showAlert,content: {
                Alert(title: Text("Message"), message: Text(alertMsg), dismissButton: .destructive(Text("OK"), action: {
                }))
            })
        }
    }
    
    func getAllPosts(){
        postRef.order(by: "startTime",descending: true).getDocuments{ snapshot,error in
            if error == nil{
                if let snapshot = snapshot{
                    DispatchQueue.main.async {
                        self.postList = snapshot.documents.map{ doc in
                            let startTStamp = doc["startTime"] as! Timestamp
                            let endTStamp = doc["startTime"] as! Timestamp


                            return Post(id:doc.documentID,
                                        postID: doc.documentID,
                                        title: doc["title"] as? String ?? "",
                                        description: doc["description"] as? String ?? "",
                                        startTime: self.getTime(timeStamp: startTStamp),
                                        endTime: self.getTime(timeStamp: endTStamp),
                                        imageURL: doc["imageURL"] as? String ?? "",
                                        documentURL: doc["documentURL"] as? String ?? "")
                        }
                    }
                }
            }else{
                DispatchQueue.main.async {
                    self.reportError(error: "No Posts Found")
                }
                return
            }
        }
    }
    
    private func getTime(timeStamp:Timestamp) -> Date{
        let date = timeStamp.dateValue()

        return Calendar.current.date(byAdding: .hour, value: -3, to: date)!
    }

    private func reportError(error: String){
        DispatchQueue.main.async {
            self.alertMsg = error
            self.showAlert.toggle()
        }
    }
    
}

struct PostListView_Previews: PreviewProvider {
    static var previews: some View {
        PostListView()
    }
}
