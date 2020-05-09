package dk.cachet.rad.application

interface SampleService {
    suspend fun rollDice(facets: Int): Int
}