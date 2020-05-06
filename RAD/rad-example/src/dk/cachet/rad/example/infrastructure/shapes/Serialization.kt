package dk.cachet.rad.example.infrastructure.shapes

import dk.cachet.rad.example.domain.shapes.Circle
import dk.cachet.rad.example.domain.shapes.Rectangle
import dk.cachet.rad.example.domain.shapes.Shape
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.EmptyModule
import kotlinx.serialization.modules.SerialModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus

val SHAPES_SERIAL_MODULE = SerializersModule {
	polymorphic(Shape::class) {
		Circle::class with Circle.serializer()
		Rectangle::class with Rectangle.serializer()
	}
}

fun createShapesSerializer(module: SerialModule = EmptyModule): Json = Json(JsonConfiguration.Stable, SHAPES_SERIAL_MODULE + module)

val JSON = createShapesSerializer()