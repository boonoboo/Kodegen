package dk.cachet.rad.serialization

import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer

@Serializer(forClass = Exception::class)

object ExceptionSerializer : KSerializer<Exception>

@Serializable
class ExceptionWrapper(@ContextualSerialization val exception: Exception)