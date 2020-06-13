package dk.cachet.kodegen.study

import dk.cachet.kodegen.study.application.AtmService
import dk.cachet.kodegen.study.domain.AccountRepository
import dk.cachet.kodegen.study.domain.Card
import dk.cachet.kodegen.study.domain.Receipt
import java.util.*

class AtmServiceImplementation(private val accountRepository: AccountRepository) : AtmService {
    override suspend fun depositMoney(card: Card, amount: Int) {
        val account = accountRepository.getAccountAssociatedWithCard(card)
        accountRepository.updateBalance(account, amount)
        return
    }

    override suspend fun retrieveMoney(card: Card, amount: Int) {
        val account = accountRepository.getAccountAssociatedWithCard(card)
        if(amount <= 0) throw IllegalArgumentException("Unable to withdraw negative amount.")
        if(account.balance <= amount) throw IllegalArgumentException("Insufficient account balance.")
        return
    }

    override suspend fun getBalance(card: Card): Int {
        val account = accountRepository.getAccountAssociatedWithCard(card)
        return account.balance
    }

    override suspend fun getReceipt(card: Card): Receipt {
        val account = accountRepository.getAccountAssociatedWithCard(card)
        return Receipt(Date(), account.balance)
    }

}