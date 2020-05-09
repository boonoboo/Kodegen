package dk.cachet.rad.infrastructure

import dk.cachet.rad.application.SampleService
import dk.cachet.rad.core.RadService
import kotlin.random.Random

@RadService
class SampleServiceImpl : SampleService {
    override suspend fun rollDice(facets: Int): Int {
        return Random.nextInt(0,6)
    }
}