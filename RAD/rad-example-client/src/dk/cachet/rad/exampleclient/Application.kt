package dk.cachet.rad.exampleclient

import dk.cachet.rad.example.application.dice.DiceService
import dk.cachet.rad.example.infrastructure.dice.rad.DiceServiceClient
import kotlinx.coroutines.runBlocking
import org.koin.core.KoinComponent
import org.koin.core.inject

fun main(args: Array<String>) {
	// TODO
	//   App should be some RAD-specific class that:
	//     Creates a Koin configuration from all modules
	//     Starts Koin

	val app = App()
	app.initialize()
}

class App() : KoinComponent {
	val diceService: DiceService by inject()

	fun initialize() {
		val diceRoll = runBlocking {
			diceService.rollDice()
		}
		print(diceRoll)
	}
}