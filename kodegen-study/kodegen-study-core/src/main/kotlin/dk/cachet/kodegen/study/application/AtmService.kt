package dk.cachet.kodegen.study.application

import dk.cachet.kodegen.ApplicationService
import dk.cachet.kodegen.RequireAuthentication
import dk.cachet.kodegen.study.domain.Card
import dk.cachet.kodegen.study.domain.Receipt

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