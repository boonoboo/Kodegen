package dk.cachet.rad.example.infrastructure.shapes

import dk.cachet.rad.core.RadService
import dk.cachet.rad.example.application.devices.ShapesService
import dk.cachet.rad.example.domain.shapes.*
import kotlin.random.Random

@RadService
class ShapesService : ShapesService {
	override suspend fun getRandomShape(): Shape {
		return when (Random.nextBoolean()) {
			true -> Circle(Random.nextDouble(10.0))
			false -> Rectangle(Random.nextDouble(10.0), Random.nextDouble(10.0))
		}
	}
}