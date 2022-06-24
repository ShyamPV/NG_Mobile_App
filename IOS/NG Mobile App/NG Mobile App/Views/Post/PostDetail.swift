//
//  PostDetail.swift
//  NG Mobile App
//
//  Created by Shyam Vekaria on 10/03/22.
//

import SwiftUI

class PostDetailViewModel: NSObject,ObservableObject, URLSessionDownloadDelegate, UIDocumentInteractionControllerDelegate{
    
    
    @Published var alertMsg:String = ""
    @Published var showAlert:Bool = false
    
    @Published var downloadtaskSession: URLSessionDownloadTask!
    
    @Published var downloadProgress: CGFloat = 0
    @Published var showDownloadProgress = false
    
    func downloadPDFDocument(documentURL: String){
        guard let docURL = URL(string: documentURL) else {
            self.reportError(error: "Invalid URL!")
            return
        }
        
        let directoryPath = FileManager.default.urls(for: .documentDirectory, in:  .userDomainMask)[0]
        
        if FileManager.default.fileExists(atPath:
                                            directoryPath.appendingPathComponent(docURL.lastPathComponent).path){
            self.showFile(filePath: directoryPath.appendingPathComponent(docURL.lastPathComponent))
            return
        }else{
            downloadProgress = 0
            withAnimation{showDownloadProgress = true}
            
            let session = URLSession(configuration: .default, delegate: self, delegateQueue: nil)
            
            downloadtaskSession = session.downloadTask(with: docURL)
            downloadtaskSession.resume()
        }
    }
    
    
    func urlSession(_ session: URLSession, downloadTask: URLSessionDownloadTask, didFinishDownloadingTo location: URL) {
        
        guard let url = downloadTask.originalRequest?.url else{
            DispatchQueue.main.async {
                self.reportError(error: "Something went wrong please try later.")
            }
            
            return
        }
        //save in document directory.
        
        let directoryPath = FileManager.default.urls(for: .documentDirectory, in:  .userDomainMask)[0]
        
        let filePath = directoryPath.appendingPathComponent(url.lastPathComponent)
        
        try? FileManager.default.removeItem(at: filePath)
        
        do{
            
            try FileManager.default.copyItem(at: location, to: filePath)
            
            DispatchQueue.main.async {
                withAnimation{self.showDownloadProgress = false}
                
                self.showFile(filePath: filePath)
            }
            
        }catch{
            DispatchQueue.main.async {
                self.reportError(error: "Something went wrong please try later.")
            }
        }
    }
    
    func urlSession(_ session: URLSession, downloadTask: URLSessionDownloadTask, didWriteData bytesWritten: Int64, totalBytesWritten: Int64, totalBytesExpectedToWrite: Int64) {
        
        let progress = CGFloat(totalBytesWritten) / CGFloat(totalBytesExpectedToWrite)
        
        DispatchQueue.main.async {
            self.downloadProgress = progress
        }
    }
    
    
    func urlSession(_ session: URLSession, task: URLSessionTask, didCompleteWithError error: Error?) {
        
        DispatchQueue.main.async {
            if let error = error {
                withAnimation{self.showDownloadProgress = false}
                self.reportError(error: error.localizedDescription)
                return
            }
        }
    }
    
    func showFile(filePath: URL) {
        let controller = UIDocumentInteractionController(url: filePath)
        
        controller.delegate = self
        controller.presentPreview(animated: true)
    }
    
    func reportError(error: String){
        DispatchQueue.main.async {
            self.alertMsg = error
            self.showAlert.toggle()
        }
    }
    
    func cancelPostDocDownload(){
        if let task = downloadtaskSession, task.state == .running{
            downloadtaskSession.cancel()
            
            DispatchQueue.main.async {
                withAnimation{self.showDownloadProgress = false}
            }
        }
    }
    
    // Fuction for presenting document view
    
    func documentInteractionControllerViewControllerForPreview(_ controller: UIDocumentInteractionController) -> UIViewController {
        
        return UIApplication.shared.windows.first!.rootViewController!
        
    }
}

struct PostDetail: View {
    @StateObject var model = PostDetailViewModel()
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
    
    static var post = Post(id: "1234", postID: "12345", title: "Test Title", description: "Test Description", startTime: Date(), endTime: Date(), imageURL: "https://firebasestorage.googleapis.com/v0/b/ng-mobile-system.appspot.com/o/Image%2FNG%202022.jpg?alt=media&token=bc4c0732-59c5-498b-9b05-1388e0de69a7", documentURL: "")
    
    static var previews: some View {
        PostDetail(post: post)
    }
}
