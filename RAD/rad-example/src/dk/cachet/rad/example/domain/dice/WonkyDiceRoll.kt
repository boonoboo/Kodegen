package dk.cachet.rad.example.domain.dice

import dk.cachet.rad.example.domain.dice.ComplexDiceRoll
import kotlinx.serialization.Serializable

@Serializable
data class WonkyDiceRoll(val eyes: Int, val complexDiceRoll: ComplexDiceRoll)