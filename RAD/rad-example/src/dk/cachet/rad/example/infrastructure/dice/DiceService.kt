package dk.cachet.rad.example.infrastructure.dice

import kotlin.random.Random
import dk.cachet.rad.core.*
import dk.cachet.rad.example.application.dice.DiceService
import dk.cachet.rad.example.domain.dice.Roll
import dk.cachet.rad.example.domain.dice.Dice
import dk.cachet.rad.example.domain.dice.WonkyRoll
import dk.cachet.rad.example.domain.dice.WonkyDice

@RadService
class DiceService : DiceService {
    override fun rollDice(): Roll {
        val dice = Dice(facets = 6)
        return Roll(Random.nextInt(1, dice.facets))
    }

    override fun rollCustomDice(dice: Dice): Roll {
        return Roll(Random.nextInt(1, dice.facets))
    }

    override fun rollDices(rolls: Int): List<Roll> {
        val diceSequence = generateSequence { rollDice() }
        return diceSequence.take(rolls).toList()
    }

    override fun rollCustomDices(dice: Dice, rolls: Int): List<Roll> {
        val diceSequence = generateSequence { rollCustomDice(dice) }
        return diceSequence.take(rolls).toList()
    }

    override fun rollWonkyDice(wonkyDice: WonkyDice): WonkyRoll {
        val rollOne = rollCustomDice(wonkyDice.diceOne)
        val rollTwo = rollCustomDice(wonkyDice.diceTwo)
        return WonkyRoll(rollOne, rollTwo)
    }

    override fun rollWonkyDices(wonkyDice: WonkyDice, rolls: Int): List<WonkyRoll> {
        val diceSequence = generateSequence { rollWonkyDice(wonkyDice) }
        return diceSequence.take(rolls).toList()
    }
}