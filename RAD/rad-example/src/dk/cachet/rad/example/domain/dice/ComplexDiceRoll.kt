package dk.cachet.rad.example.domain.dice

import kotlinx.serialization.Serializable

@Serializable
data class ComplexDiceRoll(val eyes: Int, val color: String)