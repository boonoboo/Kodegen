package dk.cachet.kodegen.study

import dk.cachet.kodegen.study.application.kodegen.AtmServiceModule
import dk.cachet.kodegen.study.infrastructure.dateSerializerModule
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.basic
import io.ktor.features.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.serialization.json
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.jetty.Jetty
import kotlinx.serialization.json.JsonConfiguration

fun main() {
    val environment = applicationEngineEnvironment {
        val accountRepository = InMemoryAccountRepository()
        val atmService = AtmServiceImplementation(accountRepository)

        module {
            mainModule()
            AtmServiceModule(service = atmService, authSchemes = *arrayOf("basic"))
        }

        connector {
            host = "0.0.0.0"
            port = 8080
        }
    }

    val server = embeddedServer(Jetty, environment)

    server.start(wait = true)
}

// Module for installing features into the web application
fun Application.mainModule() {

    // Install automatic JSON serialization using a Json handler with a custom serial module
    install(ContentNegotiation) {
        json(JsonConfiguration.Stable, dateSerializerModule)
    }

    install(Authentication)
    {
        basic(name = "basic") {
            realm = "Kodegen"
            validate { credentials ->
                if (credentials.name == "admin" && credentials.password == "adminPassword")
                    UserIdPrincipal(credentials.name)
                else {
                    null
                }
            }
        }
    }
}