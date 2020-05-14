package dk.cachet.rad.serialization

import kotlinx.serialization.*
import kotlinx.serialization.internal.HexConverter
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.JsonInput
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

//@Serializer(forClass = Exception::class)
//object ExceptionSerializer : KSerializer<Exception> {
//
//}

//@Serializable
//class ExceptionWrapper(@ContextualSerialization val exception: Throwable) : java.io.Serializable

@Serializer(forClass = Exception::class)
class ExceptionSerializer : KSerializer<Exception> {
	override val descriptor: SerialDescriptor = PrimitiveDescriptor("ExceptionWrapper", PrimitiveKind.STRING)

	@InternalSerializationApi
	override fun serialize(encoder: Encoder, value: Exception) {
		val json = Json(JsonConfiguration.Stable)

		val byteOutputStream = ByteArrayOutputStream()
		val outputStream = ObjectOutputStream(byteOutputStream)
		outputStream.writeObject(value)
		outputStream.flush()
		encoder.encodeString(HexConverter.printHexBinary(byteOutputStream.toByteArray()))
	}

	@InternalSerializationApi
	override fun deserialize(decoder: Decoder): Exception {
		println(decoder)
		val byteInputStream = ByteArrayInputStream(HexConverter.parseHexBinary(decoder.decodeString()))
		val objectInputStream = ObjectInputStream(byteInputStream)
		val output = objectInputStream.readObject() as Exception
		return output
	}
}

@Serializable
class ExceptionWrapper(@Serializable(with=ExceptionSerializer::class) val innerException: Exception) : java.io.Serializable