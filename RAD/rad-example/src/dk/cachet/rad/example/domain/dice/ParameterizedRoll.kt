package dk.cachet.rad.example.domain.dice

import kotlinx.serialization.Serializable

@Serializable
data class ParameterizedRoll<T1: Any, T2: Any>(val eyes: Int)
