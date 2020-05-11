package dk.cachet.rad.example.application.dice

import dk.cachet.rad.example.domain.dice.*

interface DiceService {
    suspend fun rollDice(): Roll

    suspend fun rollCustomDice(dice: Dice): Roll

    suspend fun rollDices(rolls: Int): List<Roll>

    suspend fun rollCustomDices(dice: Dice, rolls: Int): List<Roll>

    suspend fun rollWonkyDice(wonkyDice: WonkyDice): WonkyRoll

    suspend fun rollWonkyDices(wonkyDice: WonkyDice, rolls: Int): List<WonkyRoll>

    suspend fun rollVolatileDice(dice: Dice) : Roll

    suspend fun rollMultipleDice(dices: List<Dice>): List<Roll>

    suspend fun rollDiceAndDices(dices: Pair<List<Dice>, Dice>): Pair<List<Roll>, Roll>

    suspend fun rollClassifiedDice(dice: Dice): Roll

    suspend fun rollHiddenDice(dice: Dice)
}