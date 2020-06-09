package dk.cachet.rad.study

import dk.cachet.rad.study.infrastructure.dateSerializerModule
import dk.cachet.rad.study.application.rad.AtmServiceClient
import dk.cachet.rad.study.domain.Card
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.auth.Auth
import io.ktor.client.features.auth.providers.basic
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

fun main() {
    val app = ClientApplication()
    app.runApp()
}

class ClientApplication {
    // Initialize a Json handler with a custom serial module
    private val json = Json(JsonConfiguration.Stable, dateSerializerModule)

    // Initialize a custom Ktor HttpClient
    private val client = HttpClient(Apache) {
        install(Auth) {
            basic {
                username = "admin"
                password = "admin"
            }
        }

        install(JsonFeature) {
            serializer = KotlinxSerializer(json)
        }
    }

    // Initialize a DateService service invoker
    private val service = AtmServiceClient(client, json, "http://localhost:8080")

    fun runApp() {
        val card = Card("4571 1928 3746 5555")
        val receipt = runBlocking { service.getReceipt(card) }
        println("Balance was retrived on ${receipt.date}. Your balance is: ${receipt.balance}")
    }
}