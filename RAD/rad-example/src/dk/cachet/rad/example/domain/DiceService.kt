package dk.cachet.rad.example.domain

import kotlin.random.Random
import dk.cachet.rad.core.*

class DiceService {
    @RadMethod
    fun RollDice(): Int {
        return Random.nextInt(1, 7)
    }

    @RadMethod
    fun Ask8Ball(message: String): String {
        val answers = listOf(
            "As I see it, yes", "Ask again later", "Better not tell you now", "Cannot predict now.",
            "Concentrate and ask again.", "Don’t count on it.", "It is certain.", "It is decidedly so.",
            "Most likely.", "My reply is no.", "My sources say no.", "Outlook not so good.",
            "Outlook good.", "Reply hazy, try again.", "Signs point to yes.", "Very doubtful.", "Without a doubt.",
            "Yes.", "Yes – definitely.", "You may rely on it."
        )
        return answers.shuffled()[0]
    }

    @RadMethod
    fun RollNSidedDice(facets: Int): Int {
        return Random.nextInt(1, facets)
    }

    @RadMethod
    fun RollDices(rolls: Int): List<Int> {
        val diceSequence = generateSequence { RollDice() }
        return diceSequence.take(rolls).toList()
    }

    @RadMethod
    fun RollNSidedDices(facets: Int, rolls: Int): List<Int> {
        val diceSequence = generateSequence { RollNSidedDice(facets) }
        return diceSequence.take(rolls).toList()
    }

    @RadMethod
    fun RollColoredDice(colors: List<String>): String {
        return colors.shuffled()[0]
    }

    @RadMethod
    fun RollComplexDice(diceSpec: DiceSpecification): ComplexDiceRoll {
        var eyes: Int? = null
        var color: String? = null
        if (diceSpec.facets != null) {
            eyes = RollNSidedDice(diceSpec.facets)
        }
        if (diceSpec.colors != null) {
            color = RollColoredDice(diceSpec.colors)
        }
        return ComplexDiceRoll(eyes, color)

    }

    class DiceSpecification(val facets: Int?, val colors: List<String>?)

    class ComplexDiceRoll(val eyes: Int?, val colors: String?)
}