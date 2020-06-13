package dk.cachet.kodegen.study.domain

import dk.cachet.kodegen.study.infrastructure.DateSerializer
import java.util.*
import kotlinx.serialization.Serializable

@Serializable
data class Receipt(val date: @Serializable(with=DateSerializer::class) Date, val balance: Int)