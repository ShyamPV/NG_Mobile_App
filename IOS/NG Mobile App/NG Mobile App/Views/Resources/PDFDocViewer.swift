//
//  SwiftUIView.swift
//  NG Mobile App
//
//  Created by Shyam Vekaria on 15/03/22.
//

import SwiftUI
import PDFKit

struct PDFDocViewer: View{
    
    var url:URL?
    var membershipNo:String?
    @Environment(\.dismiss) private var dismiss
    
    var body: some View{
        VStack{
            PDFViewUI(url: url!)
            HStack{
                Spacer()
                Button {
                print("delete certificate")
                   deleteCertificate()
                } label: {
                    Text("Delete Certificate")
                        .fontWeight(.semibold)
                        .frame(width: 250, height: 40)
                        .background(Color.red)
                        .foregroundColor(.white)
                        .cornerRadius(5)
                }
                Spacer()
            }.padding(8)
        }.navigationBarTitleDisplayMode(.inline)
       
    }
    
    private func deleteCertificate(){
        guard membershipNo != "" else {
            return
        }
        
        let certificateRef = FirebaseManager.shared.storage.reference(withPath: "certificate")
        
        certificateRef.child(membershipNo!).delete { error in
            guard error == nil else{
                return
            }
            
            dismiss()
        }
    }
}

struct PDFViewUI : UIViewRepresentable {

    var url: URL?
    

    func makeUIView(context: Context) -> UIView {
        let pdfView = PDFView()

        if let url = url {
            pdfView.document = PDFDocument(url: url)
        }
        pdfView.autoScales = true

        return pdfView
    }

    func updateUIView(_ uiView: UIView, context: Context) {
        // Empty
    }

}

struct PDFDocViewer_Previews: PreviewProvider {
    static let url = URL(string: "https://firebasestorage.googleapis.com/v0/b/ng-mobile-system.appspot.com/o/certificate%2FA-02-0000?alt=media&token=a78efa3e-4095-4830-a9ba-06049a75d6d3")
    
    static var previews: some View {
        PDFViewUI(url: url!)
    }
}
