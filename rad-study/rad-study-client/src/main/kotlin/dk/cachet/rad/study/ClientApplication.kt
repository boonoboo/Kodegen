package dk.cachet.rad.study

import dk.cachet.rad.study.rad.DateServiceClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.auth.Auth
import io.ktor.client.features.auth.providers.basic
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

fun main() {
    val app = ClientApp()
    app.runApp()
}

class ClientApp {
    private val json = Json(JsonConfiguration.Stable, dateSerializerModule)

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

    private val service = DateServiceClient(client = client, json = json, baseUrl = "http://localhost:8080")

    fun runApp() {
        val dateTime = GlobalScope.async {
            service.getDateAsString("Good day!")
        }
        val date = GlobalScope.async {
            service.getDateAsDate()
        }

        runBlocking {
            println("The date today, as DateTime, is: ${dateTime.await()}")
            println("The date today, as Date, is: ${date.await()}")
        }

    }
}