package dk.cachet.kodegen.example.domain.shapes

import kotlinx.serialization.Serializable

@Serializable
class Circle(val radius: Double = 0.0): Shape() {
	override fun getArea(): Double {
		return 2 * Math.PI * radius
	}
}