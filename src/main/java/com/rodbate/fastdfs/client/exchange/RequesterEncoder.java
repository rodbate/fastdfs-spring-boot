package com.rodbate.fastdfs.client.exchange;

import io.netty.buffer.ByteBufAllocator;

import java.util.List;

/**
 *
 */
public class RequesterEncoder extends RequesterSupport {

    private final Encoder encoder;

    /**
     * @param encoder
     */
    public RequesterEncoder(Encoder encoder) {
        this.encoder = encoder;
    }

    @Override
    protected List<Object> writeRequests(ByteBufAllocator alloc) {
        return encoder.encode(alloc);
    }

    @Override
    public String toString() {
        return "RequesterEncoder{" +
                "encoder=" + encoder +
                '}';
    }
}
