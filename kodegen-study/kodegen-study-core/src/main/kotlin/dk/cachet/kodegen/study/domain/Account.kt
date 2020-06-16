package dk.cachet.kodegen.study.domain

import kotlinx.serialization.Serializable

@Serializable
class Account(val accountId: String, var balance: Int, val owner: Customer) {
    // ..domain logic
}