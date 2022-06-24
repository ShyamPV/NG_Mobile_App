//
//  LoginHeader.swift
//  ngmobile
//
//  Created by Shyam Vekaria on 28/02/22.
//

import SwiftUI

struct LoginHeader: View {
    var body: some View {
        VStack{
            Image("ng_logo_transparent").resizable().frame(width: 120, height: 150)
            Text("Nairobi Gymkhana").font(.title).fontWeight(.heavy).foregroundColor(.white)
        }.frame(maxWidth: .infinity) .frame(height: 250).background(Color.accentColor)
    }
}

struct LoginHeader_Previews: PreviewProvider {
    static var previews: some View {
        LoginHeader()
    }
}
