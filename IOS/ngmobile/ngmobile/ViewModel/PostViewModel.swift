//
//  PostViewModel.swift
//  ngmobile
//
//  Created by Shyam Vekaria on 16/02/22.
//
import UIKit
import Foundation
import Firebase
import SwiftUI

class PostViewModel: NSObject,ObservableObject, URLSessionDownloadDelegate, UIDocumentInteractionControllerDelegate{
    
    @Published var postList = [Post]()
    
    @Published var alertMsg:String = ""
    @Published var showAlert:Bool = false
    
    @Published var downloadtaskSession: URLSessionDownloadTask!
    
    @Published var downloadProgress: CGFloat = 0
    @Published var showDownloadProgress = false
    
    let postRef = Firestore.firestore().collection("post")
    
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
                self.reportError(error: "No Posts Found")
                return
            }}
    }
    
    func getTime(timeStamp:Timestamp) -> Date{
        let date = timeStamp.dateValue()
        
        return Calendar.current.date(byAdding: .hour, value: -3, to: date)!
    }
    
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
