package dk.cachet.rad.study

import dk.cachet.rad.study.rad.RollAuthenticatedDiceResponse
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.post
import io.ktor.routing.routing
import kotlinx.coroutines.runBlocking

fun Application.DateServiceModule(dateService: DateService) {
    routing {
        post("api/dateService/getDate") {
            val result = dateService.getDate()
            call.respond(result)
        }

        post("api/dateService/getDateAsString") {
            val result = dateService.getDateAsString()
            call.respond(result)
        }

        post("api/dateService/getOffsetDate") {
            val offset = call.receive<Int>()
            val result = dateService.getOffsetDate(offset)
            call.respond(result)
        }
    }
}