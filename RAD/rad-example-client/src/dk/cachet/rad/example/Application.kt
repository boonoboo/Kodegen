package dk.cachet.rad.example

import dk.cachet.rad.example.application.dice.DiceService
import dk.cachet.rad.example.application.oracle.OracleService
import dk.cachet.rad.example.application.shapes.ShapesService
import dk.cachet.rad.example.domain.dice.Dice
import dk.cachet.rad.example.infrastructure.dice.rad.DiceServiceImplClient
import dk.cachet.rad.example.infrastructure.oracle.rad.OracleServiceImplClient
import dk.cachet.rad.example.infrastructure.shapes.json
import dk.cachet.rad.example.infrastructure.shapes.rad.ShapesServiceImplClient
import io.ktor.client.HttpClient
import io.ktor.client.features.ClientRequestException
import io.ktor.client.features.HttpResponseValidator
import io.ktor.client.features.RedirectResponseException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

fun main() {
	val frontEndService = FrontEndService(
		DiceServiceImplClient(baseUrl = "http://localhost:8080"),
		OracleServiceImplClient(baseUrl = "http://localhost:8080"),
		ShapesServiceImplClient(json = json, baseUrl = "http://localhost:8080")
	)
	frontEndService.doFrontendThing()
}

class FrontEndService(private val diceService: DiceService,
					  private val oracleService: OracleService,
					  private val shapesService: ShapesService) {
	fun doFrontendThing() {
		runBlocking {
			val deferredAnswer = GlobalScope.async {
				oracleService.askOracle("Will this work?")
			}

			val deferredRollPair = GlobalScope.async {
				diceService.rollDiceAndDices(Pair(listOf(Dice(20), Dice(30)), Dice(10)))
			}

			println("The answer was \"${deferredAnswer.await().response}\" with a certainty of ${deferredAnswer.await().percentCertainty}")
			println("The lone roll of the pair was ${deferredRollPair.await().second.eyes}.")

			val shape = GlobalScope.async {
				shapesService.getRandomShape()
			}
			println(shape.await())

			GlobalScope.async {
				diceService.rollVolatileDice(Dice(10))
				diceService.rollVolatileDice(Dice(20))
				diceService.rollVolatileDice(Dice(30))
			}.await()
		}
	}
}