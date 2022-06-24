//
//  PostView.swift
//  ngmobile
//
//  Created by Shyam Vekaria on 16/02/22.
//

import SwiftUI

struct PostListView: View {
    
    @ObservedObject var model = PostViewModel()
    
    var body: some View {
        NavigationView{
            List(model.postList){post in
                NavigationLink{
                    PostDetail(post: post)
                } label: {
                    PostRow(post: post)
                }
            }.navigationTitle("Club Posts")
            Spacer()
        }
    }
    
    init(){
        model.getAllPosts()
    }
}

struct PostView_Previews: PreviewProvider {
    static var previews: some View {
        PostListView()
    }
}
