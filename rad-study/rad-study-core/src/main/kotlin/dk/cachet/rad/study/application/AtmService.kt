package dk.cachet.rad.study.application

import dk.cachet.rad.ApplicationService
import dk.cachet.rad.study.domain.Card
import dk.cachet.rad.study.domain.Receipt

@ApplicationService
interface AtmService {
    suspend fun depositMoney(card: Card, amount: Int)

    suspend fun retrieveMoney(card: Card, amount: Int)

    suspend fun getBalance(card: Card): Int

    suspend fun getReceipt(card: Card): Receipt
}