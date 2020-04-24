package dk.cachet.rad.example.infrastructure.dice

import kotlin.random.Random
import dk.cachet.rad.example.application.dice.DiceService
import dk.cachet.rad.example.domain.dice.Roll
import dk.cachet.rad.example.domain.dice.Dice
import dk.cachet.rad.example.domain.dice.WonkyRoll
import dk.cachet.rad.example.domain.dice.WonkyDice
import kotlinx.coroutines.runBlocking
import dk.cachet.rad.core.RadService

@RadService
class DiceService : DiceService {
    override suspend fun rollDice(): Roll {
        val dice = Dice(facets = 6)
        return Roll(Random.nextInt(1, dice.facets))
    }

    override suspend fun rollCustomDice(dice: Dice): Roll {
        return Roll(Random.nextInt(1, dice.facets))
    }

    override suspend fun rollDices(rolls: Int): List<Roll> {
        val diceSequence = generateSequence { runBlocking { rollDice() } }
        return diceSequence.take(rolls).toList()
    }

    override suspend fun rollCustomDices(dice: Dice, rolls: Int): List<Roll> {
        val diceSequence = generateSequence { runBlocking { rollCustomDice(dice) } }
        return diceSequence.take(rolls).toList()
    }

    override suspend fun rollWonkyDice(wonkyDice: WonkyDice): WonkyRoll {
        val rollOne = rollCustomDice(wonkyDice.diceOne)
        val rollTwo = rollCustomDice(wonkyDice.diceTwo)
        return WonkyRoll(rollOne, rollTwo)
    }

    override suspend fun rollWonkyDices(wonkyDice: WonkyDice, rolls: Int): List<WonkyRoll> {
        val diceSequence = generateSequence { runBlocking { rollWonkyDice(wonkyDice) } }
        return diceSequence.take(rolls).toList()
    }
}