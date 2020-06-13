package dk.cachet.kodegen.example

import dk.cachet.kodegen.example.application.dice.kodegen.DiceServiceClient
import dk.cachet.kodegen.example.application.oracle.kodegen.OracleServiceClient
import dk.cachet.kodegen.example.application.shapes.kodegen.ShapesServiceClient
import dk.cachet.kodegen.example.domain.dice.Dice
import dk.cachet.kodegen.example.infrastructure.shapes.json
import io.ktor.client.HttpClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

fun main() {
	val frontEndService = FrontEndService(
		DiceServiceClient(HttpClient(), baseUrl = "http://localhost:8080"),
		OracleServiceClient(HttpClient(), baseUrl = "http://localhost:8080"),
		ShapesServiceClient(HttpClient(), baseUrl = "http://localhost:8080")
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

			println("The answer was \"${deferredAnswer.await().response}\" with a certainty of ${deferredAnswer.await().percentCertainty}")

			GlobalScope.async {
				diceService.rollVolatileDice(Dice(10))
				diceService.rollVolatileDice(Dice(20))
				diceService.rollVolatileDice(Dice(30))
			}.await()
		}
	}
}