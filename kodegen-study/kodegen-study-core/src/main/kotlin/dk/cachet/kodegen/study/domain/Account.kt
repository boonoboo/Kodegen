package dk.cachet.kodegen.study.domain

import kotlinx.serialization.Serializable

@Serializable
data class Account(var balance: Int, val owner: Customer, val cards: MutableCollection<Card>)