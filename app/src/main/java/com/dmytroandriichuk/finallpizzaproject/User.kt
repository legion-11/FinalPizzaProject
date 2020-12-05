package com.dmytroandriichuk.finallpizzaproject

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User (
    var email: String? = "",
    var name:String? = ""
) {
    override fun toString(): String {
        return "User(email=$email, name=$name)"
    }
}