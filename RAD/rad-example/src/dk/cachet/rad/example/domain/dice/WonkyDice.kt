package dk.cachet.rad.example.domain.dice

import kotlinx.serialization.Serializable

@Serializable
data class WonkyDice(val diceOne: Dice, val diceTwo: Dice)