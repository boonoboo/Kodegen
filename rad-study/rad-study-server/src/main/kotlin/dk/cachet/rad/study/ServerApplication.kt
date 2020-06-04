package dk.cachet.rad.study

import dk.cachet.rad.study.rad.SampleServiceImplModule
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.basic
import io.ktor.features.ContentNegotiation
import io.ktor.serialization.json
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.jetty.Jetty


fun main() {
    val environment = applicationEngineEnvironment {
        module {
            mainModule()
            SampleServiceImplModule(service = DateServiceImpl())
        }
        connector {
            host = "0.0.0.0"
            port = 8080
        }
    }

    val server = embeddedServer(Jetty, environment)

    server.start(wait = true)
}

fun Application.mainModule() {
    install(ContentNegotiation) {
        json()
    }

    install(Authentication)
    {
        basic(name = "basic") {
            realm = "Rad Study Server"
            validate { credentials ->
                if (credentials.name == "admin" && credentials.password == "admin")
                    UserIdPrincipal(credentials.name)
                else {
                    null
                }
            }
        }
    }
}