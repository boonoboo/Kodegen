package dk.cachet.kodegen.example.application.shapes

import dk.cachet.kodegen.ApplicationService
import dk.cachet.kodegen.example.application.shapes.ShapesService
import dk.cachet.kodegen.example.domain.shapes.Circle
import dk.cachet.kodegen.example.domain.shapes.Rectangle
import dk.cachet.kodegen.example.domain.shapes.Shape
import kotlin.random.Random

class ShapesServiceImpl : ShapesService {
	override suspend fun getRandomShape(): Shape {
		return when (Random.nextBoolean()) {
			true -> Circle(Random.nextDouble(10.0))
			false -> Rectangle(Random.nextDouble(10.0), Random.nextDouble(10.0))
		}
	}
}