package dk.cachet.rad.serialization

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.EmptyModule
import kotlinx.serialization.modules.SerialModule

// TODO
//   A single serialization folder should be generated, containing:
//   createDefaultJSON method
//   ??

fun createDefaultJSON( module: SerialModule = EmptyModule ): Json
{
	val configuration = JsonConfiguration.Stable.copy(useArrayPolymorphism = true)
	return Json(configuration, module)
}

// TODO
//   A Serialization.kt file should be generated for each domain
//   Each Serialization.kt should contain:
//     A DOMAIN_SERIAL_MODULE for polymorphic types
//     A createDOMAINSerializer method
//     A JSON var for initializing the domain serializer once only

//fun createDOMAINSerializer( module: SerialModule = EmptyModule ): Json
//{
//	return createDefaultJSON(DOMAIN_SERIAL_MODULE + module)
//}
//
//   val DOMAIN_SERIAL_MODULE = SerializersModule {
//     // For each class inheriting one or more superclasses in the domain
//     polymorphic(SuperClass::class) {
//       SubClass::class with SubClass.serializer()
//     }
//   }
// var JSON: Json = createDOMAINSerializer()

