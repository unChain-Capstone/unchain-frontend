package com.unchain.data.preferences.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class SugarConsumption(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID = UUID.randomUUID(),
    val foodName: String,
    val sugarAmount: Int,
    val timestamp: Long = System.currentTimeMillis()
)

object UUIDSerializer : kotlinx.serialization.KSerializer<UUID> {
    override val descriptor: kotlinx.serialization.descriptors.SerialDescriptor
        get() = kotlinx.serialization.descriptors.PrimitiveSerialDescriptor("UUID", kotlinx.serialization.descriptors.PrimitiveKind.STRING)

    override fun serialize(encoder: kotlinx.serialization.encoding.Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: kotlinx.serialization.encoding.Decoder): UUID {
        return UUID.fromString(decoder.decodeString())
    }
}