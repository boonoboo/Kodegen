package dk.cachet.rad.example.domain.dice

import kotlinx.serialization.Serializable

@Serializable
data class WonkyDiceSpecification(val facets: Int, val diceSpecification: DiceSpecification)
