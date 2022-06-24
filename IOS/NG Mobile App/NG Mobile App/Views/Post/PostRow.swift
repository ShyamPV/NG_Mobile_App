//
//  PostRow.swift
//  NG Mobile App
//
//  Created by Shyam Vekaria on 10/03/22.
//

import SwiftUI

struct PostRow: View {
    let post:Post
    var body: some View {
        HStack{
            AsyncImage(url: URL(string: post.imageURL)){ image in
                image.resizable()
            } placeholder: {
                Image("ng_logo_transparent").resizable()
            }.frame(width: 50, height: 60).padding(10)
            VStack(alignment: .leading){
                Text(post.title)
                    .font(.headline)
                    .padding(.bottom, 5)
                Text("Date: \(post.getPostDate())")
                    .font(.subheadline)
            }
            Spacer()
        }
    }
}

struct PostRow_Previews: PreviewProvider {
    
    static var post = Post(id: "1234", postID: "12345", title: "Test Title", description: "Test Description", startTime: Date(), endTime: Date(), imageURL: "https://firebasestorage.googleapis.com/v0/b/ng-mobile-system.appspot.com/o/Image%2FNG%202022.jpg?alt=media&token=bc4c0732-59c5-498b-9b05-1388e0de69a7", documentURL: "")
    
    static var previews: some View {
        PostRow(post: post)
    }
}
