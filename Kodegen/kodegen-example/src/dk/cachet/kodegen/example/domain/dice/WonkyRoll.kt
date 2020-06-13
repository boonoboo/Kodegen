package dk.cachet.kodegen.example.domain.dice

import kotlinx.serialization.Serializable

@Serializable
data class WonkyRoll(val rollOne: Roll, val rollTwo: Roll)