package dk.cachet.rad

import dk.cachet.rad.infrastructure.rad.SampleServiceImplClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.auth.Auth
import io.ktor.client.features.auth.providers.basic
import io.ktor.client.request.post
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.SerializersModule

fun main() {
    val app = ClientApp()
    app.runApp()
}

class ClientApp {
    private val serialModule = SerializersModule {

    }

    private val json = Json(JsonConfiguration.Stable, serialModule)

    private val client = HttpClient(Apache) {
        install(Auth) {
            basic {
                username = "admin"
                password = "admin"
            }
        }
    }

    private val sampleService = SampleServiceImplClient(client = client, json = json, baseUrl = "http://localhost:8080")


    fun runApp() {
        val eyes = runBlocking {
            sampleService.rollAuthenticatedDice(10)
        }
        println(eyes)
    }
}