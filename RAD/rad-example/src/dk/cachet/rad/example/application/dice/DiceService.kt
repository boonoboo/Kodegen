package dk.cachet.rad.example.application.dice

import dk.cachet.rad.example.domain.dice.Roll
import dk.cachet.rad.example.domain.dice.Dice
import dk.cachet.rad.example.domain.dice.WonkyRoll
import dk.cachet.rad.example.domain.dice.WonkyDice

interface DiceService {
    suspend fun rollDice(): Roll

    suspend fun rollCustomDice(dice: Dice): Roll

    suspend fun rollDices(rolls: Int): List<Roll>

    suspend fun rollCustomDices(dice: Dice, rolls: Int): List<Roll>

    suspend fun rollWonkyDice(wonkyDice: WonkyDice): WonkyRoll

    suspend fun rollWonkyDices(wonkyDice: WonkyDice, rolls: Int): List<WonkyRoll>
}