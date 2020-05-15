package dk.cachet.rad.serialization

import kotlinx.serialization.*
import kotlinx.serialization.internal.HexConverter
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

@Serializer(forClass = Exception::class)
class ExceptionSerializer : KSerializer<Exception> {
	override val descriptor: SerialDescriptor = PrimitiveDescriptor("ExceptionWrapper", PrimitiveKind.STRING)

	@InternalSerializationApi
	override fun serialize(encoder: Encoder, value: Exception) {
		val byteOutputStream = ByteArrayOutputStream()
		val outputStream = ObjectOutputStream(byteOutputStream)
		outputStream.writeObject(value)
		outputStream.flush()
		encoder.encodeString(HexConverter.printHexBinary(byteOutputStream.toByteArray()))
		outputStream.close()
		byteOutputStream.close()
	}

	@InternalSerializationApi
	override fun deserialize(decoder: Decoder): Exception {
		val byteInputStream = ByteArrayInputStream(HexConverter.parseHexBinary(decoder.decodeString()))
		val objectInputStream = ObjectInputStream(byteInputStream)
		val result = objectInputStream.readObject() as Exception
		byteInputStream.close()
		objectInputStream.close()
		return result
	}
}

@Serializable
class ExceptionWrapper(@Serializable(with=ExceptionSerializer::class) val innerException: Exception) : java.io.Serializable