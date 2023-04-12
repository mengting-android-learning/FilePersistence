package com.example.filepersistencetest

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import androidx.datastore.preferences.protobuf.InvalidProtocolBufferException
import proto.CountOuterClass.Count
import java.io.InputStream
import java.io.OutputStream

object CountSerializer : Serializer<Count>{
    override val defaultValue: Count = Count.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): Count {
        try {
            return Count.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: Count, output: OutputStream) {
        t.writeTo(output)
    }

}
