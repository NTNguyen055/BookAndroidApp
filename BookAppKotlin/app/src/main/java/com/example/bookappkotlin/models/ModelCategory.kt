package com.example.bookappkotlin.models

class ModelCategory {
    // Gia tri phu hop vs firebase
    var id:String = ""
    var category:String = ""
    var timestamp:Long = 0
    var uid:String = ""

    // Ham rong
    constructor()

    // Ham tham so
    constructor(id: String, category: String, timestamp: Long, uid: String) {
        this.id = id
        this.category = category
        this.timestamp = timestamp
        this.uid = uid
    }

}