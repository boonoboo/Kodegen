package dk.cachet.rad.exampleclient

import dk.cachet.rad.core.RadConfiguration
import dk.cachet.rad.core.configureRad
import dk.cachet.rad.example.application.dice.DiceService
import dk.cachet.rad.example.domain.dice.Dice
import dk.cachet.rad.example.domain.dice.WonkyDice
import dk.cachet.rad.example.infrastructure.dice.rad.DiceServiceClient
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
	val frontEndService = FrontEndService(DiceServiceClient())
	frontEndService.doFrontendThing()
}

class FrontEndService(val diceService: DiceService = DiceServiceClient())
{
	fun doFrontendThing() {
		val roll = runBlocking {
			diceService.rollDice()
		}
		val wonkyRoll = runBlocking {
			diceService.rollWonkyDice(WonkyDice(Dice(10), Dice(20)))
		}
		println(roll.eyes)
		println("${wonkyRoll.rollOne} and ${wonkyRoll.rollTwo}")
	}
}