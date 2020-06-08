package dk.cachet.rad.study

import dk.cachet.rad.ApplicationService
import java.util.*

@ApplicationService
interface DateService {
    suspend fun getCurrentDate(): Date
}