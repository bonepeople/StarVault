package com.bonepeople.android.starvault.global.storage

import com.bonepeople.android.starvault.global.data.VaultFieldDefinitionInfo
import com.bonepeople.android.starvault.global.data.VaultFieldInfo
import com.bonepeople.android.starvault.global.data.VaultFieldSnapshotInfo
import com.bonepeople.android.starvault.global.data.VaultInfo
import com.bonepeople.android.starvault.global.data.VaultRecordInfo
import com.bonepeople.android.starvault.global.data.VaultTagDefinitionInfo
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.google.gson.stream.MalformedJsonException
import java.io.EOFException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.Reader
import java.nio.charset.CharacterCodingException
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets

object VaultPayloadCodec {
    fun write(payload: VaultPayloadInfo, output: OutputStream) {
        validatePayload(payload)
        val limitedOutput = JsonSizeLimitedOutputStream(output)
        val encoder = StandardCharsets.UTF_8.newEncoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
        val writer = JsonWriter(OutputStreamWriter(limitedOutput, encoder))
        val arrayCounter = JsonArrayCounter()
        writePayload(writer, payload, arrayCounter)
        writer.flush()
    }

    fun read(input: InputStream, expectedVaultId: String? = null): VaultPayloadInfo {
        val limitedInput = JsonSizeLimitedInputStream(input)
        val decoder = StandardCharsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
        val stringLimitedReader = JsonStringLimitedReader(InputStreamReader(limitedInput, decoder))
        val reader = JsonReader(stringLimitedReader).apply { isLenient = false }
        return try {
            val payload = readPayload(reader, JsonArrayCounter())
            expectToken(reader, JsonToken.END_DOCUMENT, "JSON document end")
            validatePayload(payload, expectedVaultId)
            payload
        } catch (exception: VaultCodecException) {
            throw exception
        } catch (exception: MalformedJsonException) {
            throw VaultCodecException.InvalidFile("Payload JSON is malformed", exception)
        } catch (exception: EOFException) {
            throw VaultCodecException.InvalidFile("Payload JSON is truncated", exception)
        } catch (exception: CharacterCodingException) {
            throw VaultCodecException.InvalidFile("Payload is not valid UTF-8", exception)
        } catch (exception: IllegalStateException) {
            throw VaultCodecException.InvalidFile("Payload JSON has an invalid value", exception)
        } catch (exception: NumberFormatException) {
            throw VaultCodecException.InvalidFile("Payload JSON number is invalid", exception)
        }
    }

    private fun writePayload(
        writer: JsonWriter,
        payload: VaultPayloadInfo,
        arrayCounter: JsonArrayCounter,
    ) {
        writer.beginObject()
        writer.name("schemaVersion").value(payload.schemaVersion.toLong())
        writer.name("revision").value(payload.revision)
        writer.name("vault")
        writeVault(writer, payload.vault, arrayCounter)
        writer.endObject()
    }

    private fun writeVault(
        writer: JsonWriter,
        vault: VaultInfo,
        arrayCounter: JsonArrayCounter,
    ) {
        writer.beginObject()
        writer.name("id").value(vault.id)
        writer.name("name").value(vault.name)
        writer.name("description").value(vault.description)
        writer.name("fieldDefinitionList").beginArray()
        vault.fieldDefinitionList.forEach { definition ->
            arrayCounter.increment()
            writeFieldDefinition(writer, definition)
        }
        writer.endArray()
        writer.name("tagDefinitionList").beginArray()
        vault.tagDefinitionList.forEach { definition ->
            arrayCounter.increment()
            writeTagDefinition(writer, definition)
        }
        writer.endArray()
        writer.name("recordList").beginArray()
        vault.recordList.forEach { record ->
            arrayCounter.increment()
            writeRecord(writer, record, arrayCounter)
        }
        writer.endArray()
        writer.endObject()
    }

    private fun writeFieldDefinition(
        writer: JsonWriter,
        definition: VaultFieldDefinitionInfo,
    ) {
        writer.beginObject()
        writer.name("id").value(definition.id)
        writer.name("name").value(definition.name)
        writer.name("description").value(definition.description)
        writer.endObject()
    }

    private fun writeTagDefinition(
        writer: JsonWriter,
        definition: VaultTagDefinitionInfo,
    ) {
        writer.beginObject()
        writer.name("id").value(definition.id)
        writer.name("name").value(definition.name)
        writer.endObject()
    }

    private fun writeRecord(
        writer: JsonWriter,
        record: VaultRecordInfo,
        arrayCounter: JsonArrayCounter,
    ) {
        writer.beginObject()
        writer.name("id").value(record.id)
        writer.name("title").value(record.title)
        writer.name("fieldList").beginArray()
        record.fieldList.forEach { field ->
            arrayCounter.increment()
            writeField(writer, field, arrayCounter)
        }
        writer.endArray()
        writer.name("tagDefinitionIdList").beginArray()
        record.tagDefinitionIdList.forEach { definitionId ->
            arrayCounter.increment()
            writer.value(definitionId)
        }
        writer.endArray()
        writer.endObject()
    }

    private fun writeField(
        writer: JsonWriter,
        field: VaultFieldInfo,
        arrayCounter: JsonArrayCounter,
    ) {
        writer.beginObject()
        writer.name("id").value(field.id)
        writer.name("definitionId").value(field.definitionId)
        writer.name("snapshotList").beginArray()
        field.snapshotList.forEach { snapshot ->
            arrayCounter.increment()
            writeSnapshot(writer, snapshot)
        }
        writer.endArray()
        writer.endObject()
    }

    private fun writeSnapshot(
        writer: JsonWriter,
        snapshot: VaultFieldSnapshotInfo,
    ) {
        writer.beginObject()
        writer.name("value").value(snapshot.value)
        writer.name("createTimestamp").value(snapshot.createTimestamp)
        writer.endObject()
    }

    private fun readPayload(
        reader: JsonReader,
        arrayCounter: JsonArrayCounter,
    ): VaultPayloadInfo {
        expectToken(reader, JsonToken.BEGIN_OBJECT, "payload object")
        reader.beginObject()
        val names = mutableSetOf<String>()
        var schemaVersion: Int? = null
        var revision: Long? = null
        var vault: VaultInfo? = null
        while (reader.hasNext()) {
            when (val name = nextUniqueName(reader, names)) {
                "schemaVersion" -> {
                    schemaVersion = nextInt(reader, name)
                    validateSchemaVersion(schemaVersion)
                }

                "revision" -> revision = nextLong(reader, name)
                "vault" -> vault = readVault(reader, arrayCounter)
                else -> throw VaultCodecException.InvalidFile("Unknown payload field: $name")
            }
        }
        reader.endObject()
        return VaultPayloadInfo(
            schemaVersion = required(schemaVersion, "schemaVersion"),
            revision = required(revision, "revision"),
            vault = required(vault, "vault"),
        )
    }

    private fun readVault(
        reader: JsonReader,
        arrayCounter: JsonArrayCounter,
    ): VaultInfo {
        expectToken(reader, JsonToken.BEGIN_OBJECT, "vault object")
        reader.beginObject()
        val names = mutableSetOf<String>()
        var id: String? = null
        var name: String? = null
        var description: String? = null
        var fieldDefinitionList: List<VaultFieldDefinitionInfo>? = null
        var tagDefinitionList: List<VaultTagDefinitionInfo>? = null
        var recordList: List<VaultRecordInfo>? = null
        while (reader.hasNext()) {
            when (val propertyName = nextUniqueName(reader, names)) {
                "id" -> id = nextString(reader, propertyName)
                "name" -> name = nextString(reader, propertyName)
                "description" -> description = nextString(reader, propertyName)
                "fieldDefinitionList" -> fieldDefinitionList = readArray(reader, arrayCounter) {
                    readFieldDefinition(reader)
                }

                "tagDefinitionList" -> tagDefinitionList = readArray(reader, arrayCounter) {
                    readTagDefinition(reader)
                }

                "recordList" -> recordList = readArray(reader, arrayCounter) {
                    readRecord(reader, arrayCounter)
                }

                else -> throw VaultCodecException.InvalidFile("Unknown vault field: $propertyName")
            }
        }
        reader.endObject()
        return VaultInfo(
            id = required(id, "vault.id"),
            name = required(name, "vault.name"),
            description = required(description, "vault.description"),
            fieldDefinitionList = required(fieldDefinitionList, "vault.fieldDefinitionList"),
            tagDefinitionList = required(tagDefinitionList, "vault.tagDefinitionList"),
            recordList = required(recordList, "vault.recordList"),
        )
    }

    private fun readFieldDefinition(reader: JsonReader): VaultFieldDefinitionInfo {
        expectToken(reader, JsonToken.BEGIN_OBJECT, "field definition object")
        reader.beginObject()
        val names = mutableSetOf<String>()
        var id: String? = null
        var name: String? = null
        var description: String? = null
        while (reader.hasNext()) {
            when (val propertyName = nextUniqueName(reader, names)) {
                "id" -> id = nextString(reader, propertyName)
                "name" -> name = nextString(reader, propertyName)
                "description" -> description = nextString(reader, propertyName)
                else -> throw VaultCodecException.InvalidFile("Unknown field definition field: $propertyName")
            }
        }
        reader.endObject()
        return VaultFieldDefinitionInfo(
            id = required(id, "fieldDefinition.id"),
            name = required(name, "fieldDefinition.name"),
            description = required(description, "fieldDefinition.description"),
        )
    }

    private fun readTagDefinition(reader: JsonReader): VaultTagDefinitionInfo {
        expectToken(reader, JsonToken.BEGIN_OBJECT, "tag definition object")
        reader.beginObject()
        val names = mutableSetOf<String>()
        var id: String? = null
        var name: String? = null
        while (reader.hasNext()) {
            when (val propertyName = nextUniqueName(reader, names)) {
                "id" -> id = nextString(reader, propertyName)
                "name" -> name = nextString(reader, propertyName)
                else -> throw VaultCodecException.InvalidFile("Unknown tag definition field: $propertyName")
            }
        }
        reader.endObject()
        return VaultTagDefinitionInfo(
            id = required(id, "tagDefinition.id"),
            name = required(name, "tagDefinition.name"),
        )
    }

    private fun readRecord(
        reader: JsonReader,
        arrayCounter: JsonArrayCounter,
    ): VaultRecordInfo {
        expectToken(reader, JsonToken.BEGIN_OBJECT, "record object")
        reader.beginObject()
        val names = mutableSetOf<String>()
        var id: String? = null
        var title: String? = null
        var fieldList: List<VaultFieldInfo>? = null
        var tagDefinitionIdList: List<String>? = null
        while (reader.hasNext()) {
            when (val propertyName = nextUniqueName(reader, names)) {
                "id" -> id = nextString(reader, propertyName)
                "title" -> title = nextString(reader, propertyName)
                "fieldList" -> fieldList = readArray(reader, arrayCounter) {
                    readField(reader, arrayCounter)
                }

                "tagDefinitionIdList" -> tagDefinitionIdList = readArray(reader, arrayCounter) {
                    nextString(reader, "tagDefinitionIdList item")
                }

                else -> throw VaultCodecException.InvalidFile("Unknown record field: $propertyName")
            }
        }
        reader.endObject()
        return VaultRecordInfo(
            id = required(id, "record.id"),
            title = required(title, "record.title"),
            fieldList = required(fieldList, "record.fieldList"),
            tagDefinitionIdList = required(tagDefinitionIdList, "record.tagDefinitionIdList"),
        )
    }

    private fun readField(
        reader: JsonReader,
        arrayCounter: JsonArrayCounter,
    ): VaultFieldInfo {
        expectToken(reader, JsonToken.BEGIN_OBJECT, "field object")
        reader.beginObject()
        val names = mutableSetOf<String>()
        var id: String? = null
        var definitionId: String? = null
        var snapshotList: List<VaultFieldSnapshotInfo>? = null
        while (reader.hasNext()) {
            when (val propertyName = nextUniqueName(reader, names)) {
                "id" -> id = nextString(reader, propertyName)
                "definitionId" -> definitionId = nextString(reader, propertyName)
                "snapshotList" -> snapshotList = readArray(reader, arrayCounter) {
                    readSnapshot(reader)
                }

                else -> throw VaultCodecException.InvalidFile("Unknown record field value: $propertyName")
            }
        }
        reader.endObject()
        return VaultFieldInfo(
            id = required(id, "field.id"),
            definitionId = required(definitionId, "field.definitionId"),
            snapshotList = required(snapshotList, "field.snapshotList"),
        )
    }

    private fun readSnapshot(reader: JsonReader): VaultFieldSnapshotInfo {
        expectToken(reader, JsonToken.BEGIN_OBJECT, "field snapshot object")
        reader.beginObject()
        val names = mutableSetOf<String>()
        var value: String? = null
        var createTimestamp: Long? = null
        while (reader.hasNext()) {
            when (val propertyName = nextUniqueName(reader, names)) {
                "value" -> value = nextString(reader, propertyName)
                "createTimestamp" -> createTimestamp = nextLong(reader, propertyName)
                else -> throw VaultCodecException.InvalidFile("Unknown field snapshot field: $propertyName")
            }
        }
        reader.endObject()
        return VaultFieldSnapshotInfo(
            value = required(value, "snapshot.value"),
            createTimestamp = required(createTimestamp, "snapshot.createTimestamp"),
        )
    }

    private inline fun <T> readArray(
        reader: JsonReader,
        arrayCounter: JsonArrayCounter,
        readItem: () -> T,
    ): List<T> {
        expectToken(reader, JsonToken.BEGIN_ARRAY, "array")
        reader.beginArray()
        val result = mutableListOf<T>()
        while (reader.hasNext()) {
            arrayCounter.increment()
            result += readItem()
        }
        reader.endArray()
        return result
    }

    private fun validatePayload(
        payload: VaultPayloadInfo,
        expectedVaultId: String? = null,
    ) {
        validateSchemaVersion(payload.schemaVersion)
        if (payload.revision < VaultFileProtocol.INITIAL_REVISION) {
            throw VaultCodecException.InvalidFile("Revision must be positive")
        }

        validateString(payload.vault.id)
        validateString(payload.vault.name)
        validateString(payload.vault.description)
        VaultFileProtocol.parseCanonicalUuid(payload.vault.id)
        if (expectedVaultId != null) {
            VaultFileProtocol.parseCanonicalUuid(expectedVaultId)
            if (payload.vault.id != expectedVaultId) {
                throw VaultCodecException.InvalidFile("Header and payload vault IDs do not match")
            }
        }

        val arrayCounter = JsonArrayCounter()
        val fieldDefinitionIds = mutableSetOf<String>()
        payload.vault.fieldDefinitionList.forEach { definition ->
            arrayCounter.increment()
            validateString(definition.id)
            validateString(definition.name)
            validateString(definition.description)
            validateUniqueUuid(definition.id, fieldDefinitionIds, "field definition")
        }

        val tagDefinitionIds = mutableSetOf<String>()
        payload.vault.tagDefinitionList.forEach { definition ->
            arrayCounter.increment()
            validateString(definition.id)
            validateString(definition.name)
            validateUniqueUuid(definition.id, tagDefinitionIds, "tag definition")
        }

        val recordIds = mutableSetOf<String>()
        val fieldIds = mutableSetOf<String>()
        payload.vault.recordList.forEach { record ->
            arrayCounter.increment()
            validateString(record.id)
            validateString(record.title)
            validateUniqueUuid(record.id, recordIds, "record")

            val recordDefinitionIds = mutableSetOf<String>()
            record.fieldList.forEach { field ->
                arrayCounter.increment()
                validateString(field.id)
                validateString(field.definitionId)
                validateUniqueUuid(field.id, fieldIds, "record field")
                VaultFileProtocol.parseCanonicalUuid(field.definitionId)
                if (field.definitionId !in fieldDefinitionIds) {
                    throw VaultCodecException.InvalidFile("Record field references a missing definition")
                }
                if (!recordDefinitionIds.add(field.definitionId)) {
                    throw VaultCodecException.InvalidFile("Record references a field definition more than once")
                }
                field.snapshotList.forEach { snapshot ->
                    arrayCounter.increment()
                    validateString(snapshot.value)
                }
            }

            val recordTagIds = mutableSetOf<String>()
            record.tagDefinitionIdList.forEach { definitionId ->
                arrayCounter.increment()
                validateString(definitionId)
                VaultFileProtocol.parseCanonicalUuid(definitionId)
                if (definitionId !in tagDefinitionIds) {
                    throw VaultCodecException.InvalidFile("Record references a missing tag definition")
                }
                if (!recordTagIds.add(definitionId)) {
                    throw VaultCodecException.InvalidFile("Record references a tag definition more than once")
                }
            }
        }
    }

    private fun validateSchemaVersion(schemaVersion: Int) {
        if (schemaVersion != VaultFileProtocol.SCHEMA_VERSION) {
            throw VaultCodecException.Unsupported("Unsupported schema version: $schemaVersion")
        }
    }

    private fun validateUniqueUuid(
        id: String,
        idSet: MutableSet<String>,
        label: String,
    ) {
        VaultFileProtocol.parseCanonicalUuid(id)
        if (!idSet.add(id)) {
            throw VaultCodecException.InvalidFile("Duplicate $label ID")
        }
    }

    private fun validateString(value: String) {
        var utf8Length = 0L
        var index = 0
        while (index < value.length) {
            val character = value[index]
            when {
                character.isHighSurrogate() -> {
                    if (index + 1 >= value.length || !value[index + 1].isLowSurrogate()) {
                        throw VaultCodecException.InvalidFile("JSON string contains an unpaired surrogate")
                    }
                    utf8Length += 4L
                    index += 2
                }

                character.isLowSurrogate() -> {
                    throw VaultCodecException.InvalidFile("JSON string contains an unpaired surrogate")
                }

                character.code <= 0x7F -> {
                    utf8Length += 1L
                    index += 1
                }

                character.code <= 0x7FF -> {
                    utf8Length += 2L
                    index += 1
                }

                else -> {
                    utf8Length += 3L
                    index += 1
                }
            }
            if (utf8Length > VaultFileProtocol.MAX_JSON_STRING_SIZE) {
                throw VaultCodecException.ResourceLimitExceeded(VaultResourceLimit.JSON_STRING_SIZE)
            }
        }
    }

    private fun nextUniqueName(
        reader: JsonReader,
        names: MutableSet<String>,
    ): String {
        val name = reader.nextName()
        if (!names.add(name)) {
            throw VaultCodecException.InvalidFile("Duplicate JSON field: $name")
        }
        return name
    }

    private fun nextString(reader: JsonReader, label: String): String {
        expectToken(reader, JsonToken.STRING, label)
        return reader.nextString()
    }

    private fun nextInt(reader: JsonReader, label: String): Int {
        expectToken(reader, JsonToken.NUMBER, label)
        return reader.nextInt()
    }

    private fun nextLong(reader: JsonReader, label: String): Long {
        expectToken(reader, JsonToken.NUMBER, label)
        return reader.nextLong()
    }

    private fun expectToken(
        reader: JsonReader,
        expectedToken: JsonToken,
        label: String,
    ) {
        if (reader.peek() != expectedToken) {
            throw VaultCodecException.InvalidFile("Expected $label")
        }
    }

    private fun <T : Any> required(value: T?, label: String): T {
        return value ?: throw VaultCodecException.InvalidFile("Missing JSON field: $label")
    }

    private class JsonArrayCounter {
        private var count = 0L

        fun increment() {
            count += 1L
            if (count > VaultFileProtocol.MAX_JSON_ARRAY_ELEMENTS) {
                throw VaultCodecException.ResourceLimitExceeded(VaultResourceLimit.JSON_ARRAY_ELEMENTS)
            }
        }
    }

    private class JsonSizeLimitedOutputStream(
        private val target: OutputStream,
    ) : OutputStream() {
        private var byteCount = 0L

        override fun write(value: Int) {
            increaseCount(1L)
            target.write(value)
        }

        override fun write(buffer: ByteArray, offset: Int, length: Int) {
            increaseCount(length.toLong())
            target.write(buffer, offset, length)
        }

        override fun flush() {
            target.flush()
        }

        private fun increaseCount(increment: Long) {
            if (increment > VaultFileProtocol.MAX_JSON_SIZE - byteCount) {
                throw VaultCodecException.ResourceLimitExceeded(VaultResourceLimit.JSON_SIZE)
            }
            byteCount += increment
        }
    }

    private class JsonSizeLimitedInputStream(
        private val source: InputStream,
    ) : InputStream() {
        private var byteCount = 0L

        override fun read(): Int {
            if (byteCount == VaultFileProtocol.MAX_JSON_SIZE) {
                return ensureEndOfInput()
            }
            val value = source.read()
            if (value != -1) byteCount += 1L
            return value
        }

        override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
            if (byteCount == VaultFileProtocol.MAX_JSON_SIZE) {
                return ensureEndOfInput()
            }
            val maximumReadLength = minOf(
                length.toLong(),
                VaultFileProtocol.MAX_JSON_SIZE - byteCount,
            ).toInt()
            val readCount = source.read(buffer, offset, maximumReadLength)
            if (readCount > 0) byteCount += readCount
            return readCount
        }

        private fun ensureEndOfInput(): Int {
            if (source.read() == -1) return -1
            throw VaultCodecException.ResourceLimitExceeded(VaultResourceLimit.JSON_SIZE)
        }
    }

    private class JsonStringLimitedReader(
        private val source: Reader,
    ) : Reader() {
        private var insideString = false
        private var escaping = false
        private var unicodeDigitsRemaining = 0
        private var unicodeValue = 0
        private var pendingHighSurrogate: Char? = null
        private var stringByteCount = 0L

        override fun read(buffer: CharArray, offset: Int, length: Int): Int {
            val readCount = source.read(buffer, offset, length)
            if (readCount == -1) {
                validateEndOfInput()
                return -1
            }
            for (index in offset until offset + readCount) {
                inspect(buffer[index])
            }
            return readCount
        }

        override fun close() {
            source.close()
        }

        private fun inspect(character: Char) {
            if (!insideString) {
                if (character == '"') {
                    insideString = true
                    stringByteCount = 0L
                }
                return
            }

            if (unicodeDigitsRemaining > 0) {
                val digit = character.digitToIntOrNull(16)
                    ?: throw VaultCodecException.InvalidFile("JSON string contains an invalid Unicode escape")
                unicodeValue = unicodeValue * 16 + digit
                unicodeDigitsRemaining -= 1
                if (unicodeDigitsRemaining == 0) {
                    accountCodeUnit(unicodeValue.toChar())
                }
                return
            }

            if (escaping) {
                escaping = false
                when (character) {
                    'u' -> {
                        unicodeDigitsRemaining = 4
                        unicodeValue = 0
                    }

                    '"', '\\', '/', 'b', 'f', 'n', 'r', 't' -> accountCodeUnit(' ')
                    else -> throw VaultCodecException.InvalidFile("JSON string contains an invalid escape")
                }
                return
            }

            when (character) {
                '\\' -> escaping = true
                '"' -> {
                    if (pendingHighSurrogate != null) {
                        throw VaultCodecException.InvalidFile("JSON string contains an unpaired surrogate")
                    }
                    insideString = false
                }

                else -> {
                    if (character.code < 0x20) {
                        throw VaultCodecException.InvalidFile("JSON string contains an unescaped control character")
                    }
                    accountCodeUnit(character)
                }
            }
        }

        private fun accountCodeUnit(character: Char) {
            val highSurrogate = pendingHighSurrogate
            if (highSurrogate != null) {
                if (!character.isLowSurrogate()) {
                    throw VaultCodecException.InvalidFile("JSON string contains an unpaired surrogate")
                }
                pendingHighSurrogate = null
                increaseStringByteCount(4L)
                return
            }

            when {
                character.isHighSurrogate() -> pendingHighSurrogate = character
                character.isLowSurrogate() -> {
                    throw VaultCodecException.InvalidFile("JSON string contains an unpaired surrogate")
                }

                character.code <= 0x7F -> increaseStringByteCount(1L)
                character.code <= 0x7FF -> increaseStringByteCount(2L)
                else -> increaseStringByteCount(3L)
            }
        }

        private fun increaseStringByteCount(increment: Long) {
            stringByteCount += increment
            if (stringByteCount > VaultFileProtocol.MAX_JSON_STRING_SIZE) {
                throw VaultCodecException.ResourceLimitExceeded(VaultResourceLimit.JSON_STRING_SIZE)
            }
        }

        private fun validateEndOfInput() {
            if (insideString || escaping || unicodeDigitsRemaining > 0 || pendingHighSurrogate != null) {
                throw VaultCodecException.InvalidFile("Payload JSON is truncated inside a string")
            }
        }
    }
}