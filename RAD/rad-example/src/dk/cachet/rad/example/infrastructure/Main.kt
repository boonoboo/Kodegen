package dk.cachet.rad.example.infrastructure

import dk.cachet.rad.core.radMain
import dk.cachet.rad.example.infrastructure.dice.DiceService
import dk.cachet.rad.example.infrastructure.dice.rad.DiceServiceModule
import dk.cachet.rad.example.infrastructure.oracle.AnswerRepository
import dk.cachet.rad.example.infrastructure.oracle.OracleService
import dk.cachet.rad.example.infrastructure.oracle.rad.OracleServiceModule
import dk.cachet.rad.example.infrastructure.shapes.ShapesService
import dk.cachet.rad.example.infrastructure.shapes.rad.ShapesServiceModule
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.authenticate
import io.ktor.auth.basic
import io.ktor.features.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.serialization.DefaultJsonConfiguration
import io.ktor.serialization.SerializationConverter
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.embeddedServer
import io.ktor.server.jetty.EngineMain
import io.ktor.server.jetty.Jetty
import kotlinx.serialization.json.Json
import org.eclipse.jetty.server.ServerConnector
import org.koin.core.context.startKoin
import org.koin.dsl.module

// Sets the main function to the startup of a Jetty engine (i.e. boots up a HTTP server)
// fun main(args: Array<String>): Unit = EngineMain.main(args)

// Sets the main function to the rad main function (configuration and engine startup)
// fun main(args: Array<String>): Unit = radMain(args)

fun main() {
	configureKoin()
	val environment = applicationEngineEnvironment {
		module {
			mainModule()
			DiceServiceModule()
			OracleServiceModule()
			ShapesServiceModule()
		}
	}
	val server = embeddedServer(Jetty, environment) {
		configureServer = {
			this.addConnector(ServerConnector(this).apply { port = 8080 })
		}
	}
	server.start(wait = true)
}
fun Application.mainModule(): Unit {
	install(ContentNegotiation) {
		register(ContentType.Application.Json, SerializationConverter(Json(DefaultJsonConfiguration)))
	}

	install(Authentication)
	{
		basic(name = "basicAuthentication") {
			realm = "SampleServer"
			validate { credentials ->
				if(credentials.name == "admin" && credentials.password == "adminP") {
					UserIdPrincipal(credentials.name)
				}
				else {
					null
				}
			}
		}
	}

	routing {
		authenticate("basicAuthentication") {
			get("/") {
				call.respondText("In root", contentType = ContentType.Text.Plain)
			}
		}
	}
}

fun configureKoin() {
	val diceModule = module {
		single { DiceService() }
	}

	val oracleModule = module {
		single<dk.cachet.rad.example.domain.oracle.AnswerRepository> { AnswerRepository() }
		single { OracleService(get()) }
	}

	val shapesModule = module {
		single { ShapesService() }
	}

	startKoin {
		modules(diceModule, oracleModule, shapesModule)
	}
}