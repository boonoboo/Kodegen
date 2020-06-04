package dk.cachet.rad.study

import io.ktor.client.HttpClient
import java.util.*

class DateServiceClient(val client: HttpClient) : DateService {

    override suspend fun getDate(): Calendar {
        TODO("Not yet implemented")
    }

}