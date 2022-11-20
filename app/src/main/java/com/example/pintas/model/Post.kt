package com.example.pintas.model

import com.google.firebase.firestore.PropertyName

data class Post(
    @get:PropertyName("postid") @set:PropertyName("postid") var postid: String = "",
    @get:PropertyName("postimage") @set:PropertyName("postimage") var postimage: String = "",
    @get:PropertyName("publisher") @set:PropertyName("publisher") var publisher: String = "",
    @get:PropertyName("description") @set:PropertyName("description") var description: String = ""
)
