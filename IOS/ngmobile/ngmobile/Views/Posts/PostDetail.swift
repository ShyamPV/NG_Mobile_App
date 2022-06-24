//
//  PostDetail.swift
//  ngmobile
//
//  Created by Shyam Vekaria on 17/02/22.
//

import SwiftUI

struct PostDetail: View {
    @StateObject var model = PostViewModel()
    let post: Post
    var body: some View {
        
        VStack(alignment: .leading) {
            ScrollView{
                HStack {
                    Spacer()
                    AsyncImage(url: URL(string: post.imageURL)){ image in
                        image.resizable().scaledToFit()
                    } placeholder: {
                        Image("ng_logo").resizable().scaledToFit()
                    }.frame( height: 200).padding(10)
                    Spacer()
                }
                HStack {
                    Spacer()
                    Text(post.title)
                        .font(.largeTitle).padding(24.0)
                    Spacer()
                }
                
                HStack {
                    Text(post.getPostDate()).padding(.leading, 24.0).padding(.vertical, 4.0)
                    Spacer()
                }
                
                
                HStack {
                    Text("From \(post.getStartTime()) To \(post.getEndTime())").padding(.leading, 24.0).padding(.vertical, 4.0)
                    Spacer()
                }
                
                
                HStack {
                    Text(post.description)
                        .padding(.horizontal, 8.0)
                    Spacer()
                }
                Spacer()
                
                
                
            }.padding(5)
            
            if(post.documentURL != ""){
                Button{
                    model.downloadPDFDocument(documentURL: post.documentURL)
                } label: {
                    Spacer()
                    Text("Get Document").frame(width: 300, height: 30).foregroundColor(Color.white).background(Color.accentColor).cornerRadius(5)
                    Spacer()
                }.padding(.bottom, 16.0)
                    .alert(isPresented: $model.showAlert,content: {
                        Alert(title: Text("Message"), message: Text(model.alertMsg), dismissButton: .destructive(Text("OK"), action: {
                            
                        }))
                    })
        
            }
            
        }.overlay(
            ZStack{
                if(model.showDownloadProgress){
                    DownloadProgessView(progress: $model.downloadProgress).environmentObject(model)
                }
            }
        )
    }
}

struct PostDetail_Previews: PreviewProvider {
    
    static var post = Post(id: "1234", postID: "12345", title: "New Years Celebration", description: "Test Description", startTime: Date(), endTime: Date(), imageURL: "https://firebasestorage.googleapis.com/v0/b/ng-mobile-system.appspot.com/o/Image%2FNG%202022.jpg?alt=media&token=bc4c0732-59c5-498b-9b05-1388e0de69a7", documentURL: "asfdgaf")
    
    static var previews: some View {
        PostDetail(post: post)
    }
}
