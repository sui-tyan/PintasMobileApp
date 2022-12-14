package com.example.pintas.model

import com.google.firebase.firestore.PropertyName

data class Comment (
    @get:PropertyName("commentid") @set:PropertyName("commentid") var commentid: String = "",
    @get:PropertyName("postid") @set:PropertyName("postid") var postid: String = "",
    @get:PropertyName("comment") @set:PropertyName("comment") var comment: String = "",
    @get:PropertyName("publisher") @set:PropertyName("publisher") var publisher: String = ""
)