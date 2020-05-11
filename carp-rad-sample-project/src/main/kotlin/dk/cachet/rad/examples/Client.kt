package dk.cachet.rad.examples

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.auth.Auth
import io.ktor.client.features.auth.providers.basic

fun main() {
    val client = HttpClient(Apache) {
        install(Auth) {
            basic {
                username = "admin"
                password = "admin"
            }
        }
    }

    val protocolServiceMockInvoker = ProtocolServiceMockClient(client = client, baseUrl = "http:\\\\localhost:8080")
}