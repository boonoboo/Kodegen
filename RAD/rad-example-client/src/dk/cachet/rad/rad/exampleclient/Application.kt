package dk.cachet.rad.rad.exampleclient

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

fun main(args: Array<String>) {
	val frontEndService = FrontEndService(DiceServiceClient(), OracleServiceClient(), ShapesServiceClient())
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

			//val deferredShape = GlobalScope.async {
			//	shapesService.getRandomShape()
			//}

			println("The roll was ${deferredRoll.await().eyes}")
			println("The answer was \"${deferredAnswer.await().response}\" with a certainty of ${deferredAnswer.await().percentCertainty}")
			/*val shape = deferredShape.await()

			if(shape is Circle) {
				println("The shape is a Circle with area ${shape.getArea()}")
			}
			else if (shape is Rectangle) {
				println("The shape is a Rectangle with area ${shape.getArea()}")
			}
			 */
		}
	}
}