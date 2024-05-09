package com.example.onlinevotingsystem
import com.google.gson.annotations.SerializedName

data class VotingModel(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var options: List<String> = listOf(),
    var votes: MutableMap<String, Int> = mutableMapOf(),
    @SerializedName("isElectionActive") var active: Boolean? = null,
    var isPublished: Boolean = false // Flag to indicate if the result has been published
)
