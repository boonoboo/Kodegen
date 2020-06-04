package dk.cachet.rad.study

import java.util.*

class DateServiceImpl : DateService {
    override suspend fun getDate(): Calendar {
        return GregorianCalendar()
    }

    // Add parameter to getDate
    // add getDateAsString
    // modify getDate to return a date as a string

    //override suspend fun getDateAsString(): String {
    //    return Date().toString()
    //}

    //override suspend fun getOffsetDate(offset: Int): Calendar {
    //    val date = GregorianCalendar()
    //    date.add(Calendar.DATE, offset)
    //    return date
    //}
}