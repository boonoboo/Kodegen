package dk.cachet.rad.study

import dk.cachet.rad.study.rad.DateServiceImplClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.auth.Auth
import io.ktor.client.features.auth.providers.basic
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.SerializersModule

fun main() {
    val app = ClientApp()
    app.runApp()
}

class ClientApp {
    private val client = HttpClient(Apache) {
        install(Auth) {
            basic {
                username = "admin"
                password = "admin"
            }
        }
    }

    private val service = DateServiceImplClient(client = client, baseUrl = "http://localhost:8080")

    fun runApp() {
        val date = runBlocking {
            service.getDate()
        }

        println("The date today is: ${date}")
    }
}