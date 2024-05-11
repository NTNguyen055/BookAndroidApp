package com.example.bookappkotlin.models

class ModelComment {
    var id = ""
    var bookId = ""
    var timestamp = ""
    var uid = ""
    var comment = ""

    constructor()

    constructor(id: String, bookId: String, timestamp: String, uid: String, comment: String) {
        this.id = id
        this.bookId = bookId
        this.timestamp = timestamp
        this.uid = uid
        this.comment = comment
    }
}