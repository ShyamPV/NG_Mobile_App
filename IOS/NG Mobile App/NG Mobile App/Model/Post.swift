//
//  Post.swift
//  NG Mobile App
//
//  Created by Shyam Vekaria on 10/03/22.
//

import Foundation
import SwiftUI

struct Post: Identifiable{

    var id: String
    var postID: String
    var title: String
    var description: String
    var startTime: Date
    var endTime: Date
    var imageURL: String
    var documentURL: String
    
    
    func getPostDate() -> String{
        return startTime.getFormattedDate(format: "dd.MM.yyyy")
    }
    
    func getStartTime() -> String{
        startTime.getFormattedDate(format: "hh:mm aa")
    }
    
    func getEndTime() -> String{
        startTime.getFormattedDate(format: "hh:mm aa")
    }
}

extension Date {
   func getFormattedDate(format: String) -> String {
        let dateformat = DateFormatter()
        dateformat.dateFormat = format
        return dateformat.string(from: self)
    }
}

