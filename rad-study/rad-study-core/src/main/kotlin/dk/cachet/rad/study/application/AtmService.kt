package dk.cachet.rad.study.application

import dk.cachet.rad.ApplicationService
import dk.cachet.rad.RequireAuthentication
import dk.cachet.rad.study.domain.Card
import dk.cachet.rad.study.domain.Receipt

@ApplicationService
interface AtmService {
    @RequireAuthentication
    suspend fun depositMoney(card: Card, amount: Int)

    @RequireAuthentication
    suspend fun retrieveMoney(card: Card, amount: Int)

    @RequireAuthentication
    suspend fun getBalance(card: Card): Int

    @RequireAuthentication
    suspend fun getReceipt(card: Card): Receipt
}