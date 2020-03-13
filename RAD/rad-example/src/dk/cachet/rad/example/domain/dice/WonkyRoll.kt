package dk.cachet.rad.example.domain.dice

import kotlinx.serialization.Serializable

@Serializable
data class WonkyRoll(val rollOne: Roll, val rollTwo: Roll)