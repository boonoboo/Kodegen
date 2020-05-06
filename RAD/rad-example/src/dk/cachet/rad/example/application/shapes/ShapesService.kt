package dk.cachet.rad.example.application.shapes

import dk.cachet.rad.example.domain.shapes.Shape

interface ShapesService
{
	suspend fun getRandomShape(): Shape
}