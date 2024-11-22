package com.unchain.data.preferences.preferences

import androidx.datastore.core.Serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.Serializable
import java.io.InputStream
import java.io.OutputStream

@Serializable
class SugarPreferencesSerializer : Serializer<SugarPreferences> {
    override val defaultValue: SugarPreferences = SugarPreferences()

    override suspend fun readFrom(input: InputStream): SugarPreferences {
        return try {
            Json.decodeFromString<SugarPreferences>(
                input.readBytes().decodeToString()
            )
        } catch (e: Exception) {
            defaultValue
        }
    }

    override suspend fun writeTo(t: SugarPreferences, output: OutputStream) {
        output.write(
            Json.encodeToString<SugarPreferences>(t).encodeToByteArray()
        )
    }
}