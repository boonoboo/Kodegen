package dk.cachet.kodegen.study.application

import dk.cachet.kodegen.ApplicationService
import dk.cachet.kodegen.RequireAuthentication
import dk.cachet.kodegen.study.domain.Account
import dk.cachet.kodegen.study.domain.Customer
import java.util.*

@ApplicationService
interface AccountService {
    @RequireAuthentication
    suspend fun create(owner: Customer): Account

    @RequireAuthentication
    suspend fun charge(account: Account, amount: Int, date: Date)

    @RequireAuthentication
    suspend fun makeTransfer(from: Account, to: Account, amount: Int)

    @RequireAuthentication
    suspend fun getAccountsByCustomer(customer: Customer): List<Account>
}