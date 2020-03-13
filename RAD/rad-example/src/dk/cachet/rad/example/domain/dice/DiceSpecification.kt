package dk.cachet.rad.example.domain.dice

import kotlinx.serialization.Serializable

@Serializable
data class DiceSpecification(val facets: Int, val colors: List<String>)
