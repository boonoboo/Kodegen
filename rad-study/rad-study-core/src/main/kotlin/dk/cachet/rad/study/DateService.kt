package dk.cachet.rad.study

import dk.cachet.carp.common.DateTime
import dk.cachet.rad.ApplicationService
import java.util.*

@ApplicationService
interface DateService {
    suspend fun getDate(): DateTime

    suspend fun getDateAsString(prefix: String): String

    suspend fun getDateAsDate(): Date
}