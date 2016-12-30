package com.rodbate.fastdfs.client.executor;

import com.rodbate.fastdfs.client.exchange.Replier;
import com.rodbate.fastdfs.client.exchange.Requester;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

import java.util.concurrent.CompletableFuture;

/**
 *
 */
final class FastdfsOperation<T> {

    private final Channel channel;
    private final Requester requester;
    private final Replier<T> replier;
    private final CompletableFuture<T> promise;

    FastdfsOperation(Channel channel, Requester requester, Replier<T> replier, CompletableFuture<T> promise) {
        this.channel = channel;
        this.requester = requester;
        this.replier = replier;
        this.promise = promise;
    }

    void execute() {

        channel.pipeline().get(FastdfsHandler.class).operation(this);
        try {

            requester.request(channel);
        } catch (Exception e) {
            caught(e);
        }
    }

    boolean isDone() {
        return promise.isDone();
    }

    void await(ByteBuf in) {
        try {

            replier.reply(in, promise);
        } catch (Exception e) {
            caught(e);
        }
    }

    void caught(Throwable cause) {
        promise.completeExceptionally(cause);
    }

    @Override
    public String toString() {
        return "FastdfsOperation{" +
                "channel=" + channel +
                ", replier=" + replier +
                ", requester=" + requester +
                '}';
    }
}
