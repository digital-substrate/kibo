// Copyright (c) Digital Substrate 2026, All rights reserved.

package com.digitalsubstrate.viper.stream;

import java.util.UUID;

public final class StreamTokenBinaryDecoder implements StreamDecoding {

    private final StreamBinaryDecoder decoder;

    public StreamTokenBinaryDecoder(byte[] data) {
        this.decoder = new StreamBinaryDecoder(data);
    }

    @Override
    public byte[] data() {
        return decoder.data();
    }

    @Override
    public int offset() {
        return decoder.offset();
    }

    @Override
    public boolean hasMore() {
        return decoder.hasMore();
    }

    @Override
    public void rewind() {
        decoder.rewind();
    }

    @Override
    public boolean readBool() throws Exception {
        readAndCheckNextToken(StreamToken.Bool);
        return decoder.readBool();
    }

    @Override
    public byte readUInt8() throws Exception {
        readAndCheckNextToken(StreamToken.UInt8);
        return decoder.readUInt8();
    }

    @Override
    public short readUInt16() throws Exception {
        readAndCheckNextToken(StreamToken.UInt16);
        return decoder.readUInt16();
    }

    @Override
    public int readUInt32() throws Exception {
        readAndCheckNextToken(StreamToken.UInt32);
        return decoder.readUInt32();
    }

    @Override
    public long readUInt64() throws Exception {
        readAndCheckNextToken(StreamToken.UInt64);
        return decoder.readUInt64();
    }

    @Override
    public byte readInt8() throws Exception {
        readAndCheckNextToken(StreamToken.Int8);
        return decoder.readInt8();
    }

    @Override
    public short readInt16() throws Exception {
        readAndCheckNextToken(StreamToken.Int16);
        return decoder.readInt16();
    }

    @Override
    public int readInt32() throws Exception {
        readAndCheckNextToken(StreamToken.Int32);
        return decoder.readInt32();
    }

    @Override
    public long readInt64() throws Exception {
        readAndCheckNextToken(StreamToken.Int64);
        return decoder.readInt64();
    }

    @Override
    public float readFloat() throws Exception {
        readAndCheckNextToken(StreamToken.Float);
        return decoder.readFloat();
    }

    @Override
    public double readDouble() throws Exception {
        readAndCheckNextToken(StreamToken.Double);
        return decoder.readDouble();
    }

    @Override
    public UUID readUuid() throws Exception {
        readAndCheckNextToken(StreamToken.Uuid);
        return decoder.readUuid();
    }

    @Override
    public String readString() throws Exception {
        readAndCheckNextToken(StreamToken.String);
        return decoder.readString();
    }

    @Override
    public byte[] readBlob() throws Exception {
        readAndCheckNextToken(StreamToken.Blob);
        return decoder.readBlob();
    }

    private byte readToken() throws Exception {
        var rawValue = decoder.readUInt8();
        return checkTokenRawValue(rawValue);
    }

    private byte checkTokenRawValue(byte rawValue) throws Exception {
        if (rawValue > StreamToken.LAST)
            throw new StreamErrorsUnknownToken(rawValue);

        return rawValue;
    }

    private void readAndCheckNextToken(byte token) throws Exception {
        var t = readToken();
        if (t != token)
            throw new StreamErrorsUnexpectedToken(t, token);
    }
}
