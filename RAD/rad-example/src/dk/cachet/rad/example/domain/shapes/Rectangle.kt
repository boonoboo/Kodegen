package dk.cachet.rad.example.domain.shapes

import kotlinx.serialization.Serializable

@Serializable
open class Rectangle(val length: Double = 0.0, val width: Double = 0.0) : Shape() {
	override fun getArea(): Double {
		return length*width
	}
}