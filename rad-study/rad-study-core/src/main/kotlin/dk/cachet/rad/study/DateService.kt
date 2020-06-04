package dk.cachet.rad.study

import dk.cachet.rad.RadService
import java.util.*

@RadService
interface DateService {
    suspend fun getDate(): Calendar

    // suspend fun getDateAsString(): String

    // suspend fun getOffsetDate(): Int
}