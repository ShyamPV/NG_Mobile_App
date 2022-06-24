//
//  StatementView.swift
//  NG Mobile App
//
//  Created by Shyam Vekaria on 21/03/22.
//

import SwiftUI

struct StatementView: View {
    
    var member:Member?
    
    var body: some View {
        
        let dueYear = "2023"
        let subsAmount = 15004
        let sportsAmount = 696
        let total = subsAmount + sportsAmount
        
        ScrollView{
            VStack{
                    VStack(){
                        Image("ng_logo").resizable()
                            .frame(width: 70, height: 88)
                        Text("Nairobi Gymkhana")
                        Text("P.O.Box 40895 - 00100")
                        Text("0727531457/8")
                        Text("info@nairobigymkhana.com")
                        Text("Annual Statement")
                            .font(.title)
                            .padding(.vertical)
                    }
                HStack {
                    VStack(alignment: .leading){
                        Text("For: Club Subscription "+dueYear)
                        Text("Due By: 28/02/"+dueYear)
                            .fontWeight(.bold).padding(.top, 5)
                    }
                    Spacer()
                }.padding(.leading,5)
                HStack {
                    VStack(alignment: .leading){
                        Text("Subscripitons: Ksh " + String(subsAmount))
                        Text("Sports Levy: Ksh " + String(sportsAmount))
                    }
                    Spacer()
                }.padding(5)
                HStack{
                    Spacer()
                    Text("Total: Ksh" + String(total))
                }.padding(.trailing,5)
            }
        }.padding(5)
    }
    
    
    
    
}

struct StatementView_Previews: PreviewProvider {
    
    static var previews: some View {
        StatementView()
    }
}
