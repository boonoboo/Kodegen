package dk.cachet.kodegen.example

import kotlin.random.Random
import dk.cachet.kodegen.example.application.dice.DiceService
import kotlinx.coroutines.runBlocking
import dk.cachet.kodegen.example.domain.dice.*

class DiceServiceImpl : DiceService {
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

    override suspend fun rollVolatileDice(dice: Dice): Roll {
        if(Random.nextBoolean()){
            throw IllegalArgumentException()
        }
        else return rollCustomDice(dice)
    }

    override suspend fun rollMultipleDice(dices: List<Dice>): List<Roll> {
        return dices.map { dice -> rollCustomDice(dice) }
    }

    override suspend fun rollDiceAndDices(dices: Pair<List<Dice>, Dice>): Pair<List<Roll>, Roll> {
        return Pair(dices.first.map { dice -> rollCustomDice(dice) }, rollCustomDice(dices.second))
    }

    override suspend fun rollClassifiedDice(dice: Dice): Roll {
        return rollCustomDice(dice)
    }

    override suspend fun rollHiddenDice(dice: Dice) {
        return
    }
}