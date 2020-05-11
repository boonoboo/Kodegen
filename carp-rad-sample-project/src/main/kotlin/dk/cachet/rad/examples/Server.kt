package dk.cachet.rad.examples

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.basic
import io.ktor.features.ContentNegotiation
import io.ktor.serialization.json

fun main() {

}

fun Application.mainModule() {
    install(ContentNegotiation) {
        json()
    }

    install(Authentication) {
        basic("basic") {
            validate { credentials ->
                if(credentials.name == "admin" && credentials.password == "admin") {
                    UserIdPrincipal(credentials.name)
                }
                else {
                    null
                }
            }
        }
    }
}