package dk.cachet.rad.example

import kotlin.random.Random
import dk.cachet.rad.core.*

class Main {
    fun main(vararg args: String) {
        println("Hello from main")
    }


}

fun RollDice(): Int {
    return Random.nextInt(1,7)
}

@RadMethod
fun RollMultipliedDice(factor: Int): Int {
    return RollDice() * factor
}