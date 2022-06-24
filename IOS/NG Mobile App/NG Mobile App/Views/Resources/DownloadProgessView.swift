//
//  DownloadProgessView.swift
//  NG Mobile App
//
//  Created by Shyam Vekaria on 10/03/22.
//

import SwiftUI

struct DownloadProgessView: View {
    
    @Binding var progress: CGFloat
    @EnvironmentObject var model: PostDetailViewModel
    
    var body: some View {
        ZStack{
            Color.primary.opacity(0.25)
                .ignoresSafeArea()
            
            VStack(spacing: 15){
                
                ZStack {
                    Circle().fill(Color.gray.opacity(0.3))
                    ProgressShape(progress: progress).fill(Color.gray.opacity(0.45)).rotationEffect(.init(degrees:  -90))
                    
                }.frame(width: 70, height: 70)
                    
                Button{
                    model.cancelPostDocDownload()
                } label: {
                    Text("Cancel").fontWeight(.semibold)
                }.padding(.top)
            }
            .padding(.vertical, 20)
            .padding(.horizontal,50)
            .background(Color.white)
            .cornerRadius(8)
        }
    }
}

struct DownloadProgessView_Previews: PreviewProvider {
    static var previews: some View {
        DownloadProgessView(progress: .constant(0.5))
    }
}

struct ProgressShape: Shape{
    
    var progress: CGFloat
    
    func path(in rect: CGRect) -> Path {
        return Path{ path in

            path.move(to:  CGPoint(x: rect.midX, y: rect.midY))
            
            path.addArc(center: CGPoint(x: rect.midX, y: rect.midY), radius: 35, startAngle: .zero, endAngle: .init(degrees: Double(progress * 360)), clockwise: false)
        }
    }
}
