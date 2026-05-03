// Copyright (c) Digital Substrate 2026, All rights reserved.

package com.digitalsubstrate.viper.stream;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class StreamBinaryDecoder implements StreamDecoding {

    private final byte[] data;
    private final ByteBuffer byteBuffer;

    public StreamBinaryDecoder(byte[] data) {
        this.data = data;
        this.byteBuffer = ByteBuffer.wrap(data);
        this.byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    // Stream Decoding
    @Override
    public byte[] data() {
        return data;
    }

    @Override
    public int offset() {
        return this.byteBuffer.arrayOffset();
    }

    @Override
    public boolean hasMore() {
        return this.byteBuffer.hasRemaining();
    }

    @Override
    public void rewind() {
        this.byteBuffer.rewind();
    }

    // Stream Reading
    @Override
    public boolean readBool() throws Exception {
        checkEOS(1);
        var b = this.byteBuffer.get();
        return b != 0;
    }

    @Override
    public byte readUInt8() throws Exception {
        checkEOS(1);
        return byteBuffer.get();
    }

    @Override
    public short readUInt16() throws Exception {
        checkEOS(2);
        return byteBuffer.getShort();
    }

    @Override
    public int readUInt32() throws Exception {
        checkEOS(4);
        return byteBuffer.getInt();
    }

    @Override
    public long readUInt64() throws Exception {
        checkEOS(8);
        return byteBuffer.getLong();
    }

    @Override
    public byte readInt8() throws Exception {
        checkEOS(1);
        return byteBuffer.get();
    }

    @Override
    public short readInt16() throws Exception {
        checkEOS(2);
        return byteBuffer.getShort();
    }

    @Override
    public int readInt32() throws Exception {
        checkEOS(4);
        return byteBuffer.getInt();
    }

    @Override
    public long readInt64() throws Exception {
        checkEOS(8);
        return byteBuffer.getLong();
    }

    @Override
    public float readFloat() throws Exception {
        checkEOS(4);
        return byteBuffer.getFloat();
    }

    @Override
    public double readDouble() throws Exception {
        checkEOS(8);
        return byteBuffer.getDouble();
    }

    @Override
    public String readString() throws Exception {
        var count = (int) readUInt64();
        checkEOS(count);

        if (count == 0)
            return "";

        byte[] utf8 = new byte[(int) count];
        byteBuffer.get(utf8);

        return new String(utf8, StandardCharsets.UTF_8);
    }

    @Override
    public byte[] readBlob() throws Exception {
        var count = (int) readUInt64();
        checkEOS(count);

        if (count == 0)
            return new byte[0];

        var result = new byte[(int) count];
        byteBuffer.get(result);

        return result;
    }

    @Override
    public UUID readUuid() throws Exception {
        checkEOS(16);

        long firstLong = Long.reverseBytes(this.byteBuffer.getLong());
        long secondLong = Long.reverseBytes(this.byteBuffer.getLong());

        return new UUID(firstLong, secondLong);
    }

    private void checkEOS(int size) throws Exception {
        if (byteBuffer.remaining() < size)
            throw new StreamErrorsPrematureEndOfStream();
    }

}
