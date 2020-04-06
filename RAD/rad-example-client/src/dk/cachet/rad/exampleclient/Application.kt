package dk.cachet.rad.exampleclient

import dk.cachet.rad.example.application.dice.DiceService
import dk.cachet.rad.example.application.oracle.OracleService
import dk.cachet.rad.example.domain.dice.Dice
import dk.cachet.rad.example.domain.dice.WonkyDice
import dk.cachet.rad.example.infrastructure.dice.rad.DiceServiceClient
import dk.cachet.rad.example.infrastructure.oracle.rad.OracleServiceClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
	val frontEndService = FrontEndService(DiceServiceClient(), OracleServiceClient())
	frontEndService.doFrontendThing()
}

class FrontEndService(private val diceService: DiceService, private val oracleService: OracleService)
{
	fun doFrontendThing() {
		runBlocking {
			val roll = GlobalScope.async {
				diceService.rollCustomDice(Dice(100))
			}

			val answer = GlobalScope.async {
				oracleService.askOracle("Will this work?")
			}
			println("The roll was ${roll.await().eyes}")
			println("The answer was \"${answer.await().response}\" with a certainty of ${answer.await().percentCertainty}")
		}
	}
}