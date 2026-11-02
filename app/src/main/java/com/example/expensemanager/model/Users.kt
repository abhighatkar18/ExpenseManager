package com.example.expensemanager.model

class Users {
    var uname:String?=null
    var email:String?=null
    var password:String?=null
    constructor()
    constructor(uname:String?,email:String?,password:String?){
        this.uname=uname
        this.email=email
        this.password=password
    }

    constructor(email: String?, password: String?) {
        this.email = email
        this.password = password
    }

}