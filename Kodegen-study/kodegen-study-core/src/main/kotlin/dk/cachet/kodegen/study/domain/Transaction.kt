package dk.cachet.kodegen.study.domain

import dk.cachet.kodegen.study.infrastructure.DateSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
class Transaction(val id: String, val amount: Int, val account: Account, val date: @Serializable(with=DateSerializer::class) Date) {
    // ..domain logic
}