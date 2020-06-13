package dk.cachet.kodegen.example.application.shapes

import dk.cachet.kodegen.ApplicationService
import dk.cachet.kodegen.example.domain.shapes.Shape

@ApplicationService
interface ShapesService
{
	suspend fun getRandomShape(): Shape
}