package com.example.onlinevotingsystem

data class Registration(
    var userId: String = "",
    var age: Int = 0,
    var voterId: String = "",
    var isVerified: Boolean = false
) {
    // Default constructor
    constructor() : this("", 0, "", false)
}
