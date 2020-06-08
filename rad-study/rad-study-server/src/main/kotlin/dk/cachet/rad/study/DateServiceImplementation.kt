package dk.cachet.rad.study

import java.util.*

class DateServiceImplementation() : DateService {
    override suspend fun getCurrentDate(): Date {
        return Date()
    }
}