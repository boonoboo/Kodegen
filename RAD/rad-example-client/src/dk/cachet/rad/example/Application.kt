package dk.cachet.rad.example

import dk.cachet.rad.example.application.dice.rad.DiceServiceClient
import dk.cachet.rad.example.application.oracle.rad.OracleServiceClient
import dk.cachet.rad.example.application.shapes.rad.ShapesServiceClient
import dk.cachet.rad.example.domain.dice.Dice
import dk.cachet.rad.example.infrastructure.shapes.json
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

fun main() {
	val frontEndService = FrontEndService(
		DiceServiceClient(baseUrl = "http://localhost:8080"),
		OracleServiceClient(baseUrl = "http://localhost:8080"),
		ShapesServiceClient(json = json, baseUrl = "http://localhost:8080")
	)
	frontEndService.doFrontendThing()
}

class FrontEndService(private val diceService: DiceServiceClient,
					  private val oracleService: OracleServiceClient,
					  private val shapesService: ShapesServiceClient) {
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