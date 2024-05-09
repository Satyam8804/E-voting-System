package com.example.onlinevotingsystem

data class User(
    var name: String? = null,
    var email: String? = null,
    var pass: String? = null,
    var image: String? = null,
    var votedInVotings: MutableMap<String, Boolean> = mutableMapOf()
) {
    fun hasVotedIn(votingId: String): Boolean {
        return votedInVotings[votingId] ?: false
    }

    fun voteInVoting(votingId: String) {
        if (!hasVotedIn(votingId)) {
            votedInVotings[votingId] = true
        }
    }
}

