package dk.cachet.rad.example.domain.dice

import kotlin.random.Random
import dk.cachet.rad.core.*
import dk.cachet.rad.example.domain.oracle.OracleService

@RadService
class DiceService {
    fun rollDice(): Int {
        return Random.nextInt(1, 7)
    }

    fun rollNSidedDice(facets: Int): Int {
        return Random.nextInt(1, facets)
    }

    fun rollDices(rolls: Int): List<Int> {
        val diceSequence = generateSequence { rollDice() }
        return diceSequence.take(rolls).toList()
    }

    fun rollNSidedDices(facets: Int, rolls: Int): List<Int> {
        val diceSequence = generateSequence { rollNSidedDice(facets) }
        return diceSequence.take(rolls).toList()
    }

    fun rollColoredDice(colors: List<String>): String {
        return colors.shuffled()[0]
    }

    fun rollComplexDice(diceSpec: DiceSpecification): ComplexDiceRoll {
        val eyes = rollNSidedDice(diceSpec.facets)
        val color = rollColoredDice(diceSpec.colors)
        return ComplexDiceRoll(eyes, color)
    }

    fun rollWonkyDice(wonkyDiceSpec: WonkyDiceSpecification): WonkyDiceRoll {
        val eyes = rollNSidedDice(wonkyDiceSpec.facets)
        val complexDiceRoll = rollComplexDice(wonkyDiceSpec.diceSpecification)
        return WonkyDiceRoll(eyes, complexDiceRoll)
    }
}