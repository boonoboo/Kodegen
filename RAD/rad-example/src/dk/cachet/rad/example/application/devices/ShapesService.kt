package dk.cachet.rad.example.application.devices

import dk.cachet.rad.example.domain.shapes.Shape

interface ShapesService
{
	suspend fun getRandomShape(): Shape
}