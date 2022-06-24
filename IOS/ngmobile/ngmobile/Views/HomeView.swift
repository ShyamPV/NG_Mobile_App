//
//  HomeView.swift
//  ngmobile
//
//  Created by Shyam Vekaria on 16/02/22.
//

import SwiftUI
import Firebase


struct HomeView: View {
    
    var body: some View {
        VStack{
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
        .font(.headline)
        }
    }
}

struct HomeView_Previews: PreviewProvider {
    
    static var previews: some View {
        
        HomeView()
    }
}
