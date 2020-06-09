package dk.cachet.rad.study

import dk.cachet.rad.study.domain.*
import io.ktor.features.NotFoundException

class InMemoryAccountRepository : AccountRepository {
    override fun getAccountAssociatedWithCard(card: Card): Account {
        if(card.id=="4571 1928 3746 5555") return Account(1000, Customer("Charlie"), mutableListOf(Card("4571 1928 3746 5555")))
        else throw NotFoundException("No card with the given ID exists.")
    }

    override fun updateBalance(account: Account, amount: Int): Int {
        account.balance += amount
        return account.balance
    }
}