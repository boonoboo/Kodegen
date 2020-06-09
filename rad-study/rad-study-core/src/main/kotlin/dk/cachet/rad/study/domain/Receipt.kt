package dk.cachet.rad.study.domain

import dk.cachet.rad.study.infrastructure.DateSerializer
import java.util.*
import kotlinx.serialization.Serializable

@Serializable
data class Receipt(val date: @Serializable(with=DateSerializer::class) Date, val balance: Int)