package dk.cachet.rad.example.server

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.serialization.json

// Sets the main function to the startup of a Jetty engine (i.e. boots up a HTTP server)
fun main(args: Array<String>): Unit = io.ktor.server.jetty.EngineMain.main(args)

fun Application.mainModule() {
	install(ContentNegotiation) {
		json()
	}

	routing {
		get("/") {
			call.respondText("In root", contentType = ContentType.Text.Plain)
		}
	}
}