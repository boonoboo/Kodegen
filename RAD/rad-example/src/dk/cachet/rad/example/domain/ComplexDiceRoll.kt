package dk.cachet.rad.example.domain

import kotlinx.serialization.Serializable

@Serializable
data class ComplexDiceRoll(val eyes: Int, val color: String)