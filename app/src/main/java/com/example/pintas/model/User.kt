package com.example.pintas.model

import com.google.firebase.firestore.PropertyName

data class User (
    @get:PropertyName("username") @set:PropertyName("username") var username: String = "",
    @get:PropertyName("email") @set:PropertyName("email") var email: String = "",
    @get:PropertyName("followers") @set:PropertyName("followers") var followers: String = "",
    @get:PropertyName("uid") @set:PropertyName("uid") var uid: String = "",
    @get:PropertyName("image") @set:PropertyName("image") var image: String = ""
)