package dk.cachet.rad.serialization

import kotlinx.serialization.*

// Originally from dk.cachet.carp.common.serialization
// Hosted at the time of writing at https://github.com/cph-cachet/carp.core-kotlin

/**
 * A wrapper for objects which need to be serialized using a different serializer than the one configured at compile time.
 * When the [Encoder] encounters this wrapper, [inner] will be serialized using [serializer].
 */
@Serializable
class SerializerWrapper internal constructor(val inner: Any, val serializer: KSerializer<Any>) {
	@Serializer(forClass = SerializerWrapper::class)
	companion object : KSerializer<SerializerWrapper>
	{
		override val descriptor: SerialDescriptor
			get() = PrimitiveDescriptor("inner", PrimitiveKind.STRING)

		override fun serialize( encoder: Encoder, value: SerializerWrapper) =
			encoder.encode( value.serializer, value.inner )

		override fun deserialize( decoder: Decoder ): SerializerWrapper =
			throw UnsupportedOperationException( "${SerializerWrapper::class.simpleName} only supports serialization." )
	}
}

/**
 * Specify a custom [serializer] to use for [inner].
 * When the [Encoder] encounters this wrapper, [inner] will be serialized using [serializer] instead.
 */
@Suppress("UNCHECKED_CAST")
fun <T : Any> serializerWrapper(inner: T, serializer: KSerializer<T>): SerializerWrapper
{
	return SerializerWrapper(inner, serializer as KSerializer<Any>)
}