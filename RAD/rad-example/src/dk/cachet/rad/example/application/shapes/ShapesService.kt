package dk.cachet.rad.example.application.shapes

import dk.cachet.rad.ApplicationService
import dk.cachet.rad.example.domain.shapes.Shape

@ApplicationService
interface ShapesService
{
	suspend fun getRandomShape(): Shape
}