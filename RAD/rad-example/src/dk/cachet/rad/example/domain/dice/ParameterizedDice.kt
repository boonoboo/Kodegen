package dk.cachet.rad.example.domain.dice

import kotlinx.serialization.Serializable

@Serializable
data class ParameterizedDice<T1: Any, T2: Any>(val facets: Int)
