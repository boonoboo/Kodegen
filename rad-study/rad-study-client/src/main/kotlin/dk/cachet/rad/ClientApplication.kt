package dk.cachet.rad

import dk.cachet.rad.infrastructure.rad.SampleServiceImplClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.auth.Auth
import io.ktor.client.features.auth.providers.basic

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonConfiguration

fun main() {

    val client = HttpClient(Apache) {
        install(Auth) {
            basic {
                username = "admin"
                password = "admin"
            }
        }
    }

    val sampleService = SampleServiceImplClient(client = client, baseUrl = "http://localhost:8080")
    val eyes = runBlocking {
        sampleService.rollAuthenticatedDice(10)
    }
    println(eyes)
}