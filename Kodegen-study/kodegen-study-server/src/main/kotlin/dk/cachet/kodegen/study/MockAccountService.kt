package dk.cachet.kodegen.study

import dk.cachet.kodegen.study.application.AccountService
import dk.cachet.kodegen.study.domain.*
import java.util.*

class MockAccountService : AccountService {
    override suspend fun create(owner: Customer): Account {
        return Account("0", 0, owner)
    }

    override suspend fun charge(account: Account, amount: Int, date: Date) {
        return
    }

    override suspend fun makeTransfer(from: Account, to: Account, amount: Int) {
        return
    }

    override suspend fun getAccountsByCustomer(customer: Customer): List<Account> {
        return emptyList()
    }


}