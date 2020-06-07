package dk.cachet.rad.study

import dk.cachet.carp.common.DateTime
import java.util.*

class DateServiceImpl : DateService {
    override suspend fun getDate(): Date {
        return Date()
    }
}