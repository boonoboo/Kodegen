package dk.cachet.kodegen.study

import dk.cachet.kodegen.study.application.kodegen.AccountServiceClient
import dk.cachet.kodegen.study.application.kodegen.CustomerServiceClient
import dk.cachet.kodegen.study.infrastructure.dateSerializerModule
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
                password = "adminPassword"
            }
        }

        install(JsonFeature) {
            serializer = KotlinxSerializer(json)
        }
    }

    private val accountService = AccountServiceClient(client, "http://localhost:8080")
    private val customerService = CustomerServiceClient(client, "http://localhost:8080")

    fun runApp() {
        val customer = runBlocking { customerService.getCustomerById("0") }
        val accounts = runBlocking { accountService.getAccountsByCustomer(customer) }
    }
}