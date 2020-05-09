package dk.cachet.rad.example

import dk.cachet.rad.example.application.shapes.ShapesService
import dk.cachet.rad.example.application.dice.DiceService
import dk.cachet.rad.example.application.oracle.OracleService
import dk.cachet.rad.example.domain.dice.Dice
import dk.cachet.rad.example.infrastructure.dice.rad.DiceServiceClient
import dk.cachet.rad.example.infrastructure.oracle.rad.OracleServiceClient
import dk.cachet.rad.example.infrastructure.shapes.rad.ShapesServiceClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.lang.IllegalStateException

fun main(args: Array<String>) {
	val frontEndService = FrontEndService(
		DiceServiceClient(),
		OracleServiceClient(),
		ShapesServiceClient()
	)
	frontEndService.doFrontendThing()
}

class FrontEndService(private val diceService: DiceService, private val oracleService: OracleService, private val shapesService: ShapesService)
{
	fun doFrontendThing() {
		runBlocking {
			val deferredRoll = GlobalScope.async {
				diceService.rollCustomDice(Dice(100))
			}

			val deferredAnswer = GlobalScope.async {
				oracleService.askOracle("Will this work?")
			}

			val deferredRollPair = GlobalScope.async {
				diceService.rollDiceAndDices(Pair(listOf(Dice(20), Dice(30)), Dice(10)))
			}

			println("The roll was ${deferredRoll.await().eyes}")
			println("The answer was \"${deferredAnswer.await().response}\" with a certainty of ${deferredAnswer.await().percentCertainty}")
			println("The lone roll of the pair was ${deferredRollPair.await().second.eyes}.")
			try {
				val volatileRoll = GlobalScope.async {
					diceService.rollVolatileDice(Dice(10))
				}.await()
				println("The volatile roll was succesfull! Value is ${volatileRoll.eyes}")
			}
			catch (exception: Exception){
				when(exception) {
					is IllegalStateException -> {
						println("Exception was caught!")
						println(exception)
					}
					else -> throw exception
				}

			}
		}
	}
}