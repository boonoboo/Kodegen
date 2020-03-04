package dk.cachet.rad.example.domain

import kotlin.random.Random
import dk.cachet.rad.core.*

@RadMethod
fun RollDice(): Int {
    return Random.nextInt(1,7)
}

@RadMethod
fun RollNSidedDice(facets: Int): Int {
    return Random.nextInt(1,facets)
}