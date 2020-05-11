package dk.cachet.rad.example.infrastructure.shapes

import dk.cachet.rad.core.RadService
import dk.cachet.rad.example.application.shapes.ShapesService
import dk.cachet.rad.example.domain.shapes.Circle
import dk.cachet.rad.example.domain.shapes.Rectangle
import dk.cachet.rad.example.domain.shapes.Shape
import kotlin.random.Random

@RadService
class ShapesServiceImpl : ShapesService {
	override suspend fun getRandomShape(): Shape {
		return when (Random.nextBoolean()) {
			true -> Circle(Random.nextDouble(10.0))
			false -> Rectangle(Random.nextDouble(10.0), Random.nextDouble(10.0))
		}
	}
}