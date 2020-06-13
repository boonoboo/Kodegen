package dk.cachet.kodegen.example.domain.shapes

import kotlinx.serialization.Serializable
import kotlinx.serialization.Polymorphic

@Serializable
@Polymorphic
abstract class Shape {
	abstract fun getArea(): Double
}