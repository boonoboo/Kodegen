package dk.cachet.rad.study

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.post
import io.ktor.routing.routing

fun Application.CustomDateServiceModule(dateService: DateService) {
    routing {
        post("radApi/dateService/getDate") {
            val result = dateService.getDate()
            call.respond(result)
        }
    }
}