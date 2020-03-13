package dk.cachet.rad.example.application.dice

import dk.cachet.rad.example.domain.dice.Roll
import dk.cachet.rad.example.domain.dice.Dice
import dk.cachet.rad.example.domain.dice.WonkyRoll
import dk.cachet.rad.example.domain.dice.WonkyDice

interface DiceService {
    fun rollDice(): Roll

    fun rollCustomDice(dice: Dice): Roll

    fun rollDices(rolls: Int): List<Roll>

    fun rollCustomDices(dice: Dice, rolls: Int): List<Roll>

    fun rollWonkyDice(wonkyDice: WonkyDice): WonkyRoll

    fun rollWonkyDices(wonkyDice: WonkyDice, rolls: Int): List<WonkyRoll>
}