/**
 *
 */
package com.rodbate.fastdfs.client.executor;



import com.rodbate.fastdfs.client.exchange.*;
import com.rodbate.fastdfs.client.exchange.Requester;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

/**
 * FastdfsExecutor
 *
 * @author liulongbiao
 */
public final class FastdfsExecutor implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(FastdfsExecutor.class);

    private final EventLoopGroup loopGroup;
    private final FastdfsPoolGroup poolGroup;

    public FastdfsExecutor(FastdfsSettings settings) {
        loopGroup = new NioEventLoopGroup(settings.maxThreads());
        poolGroup = new FastdfsPoolGroup(
                loopGroup,
                settings.connectTimeout(),
                settings.readTimeout(),
                settings.idleTimeout(),
                settings.maxConnPerHost()
        );
    }

    /**
     * 访问 Fastdfs 服务器
     *
     * @param addr
     * @param encoder
     * @param decoder
     * @return
     */
    public <T> CompletableFuture<T> execute(InetSocketAddress addr, Requester.Encoder encoder, Replier.Decoder<T> decoder) {
        return execute(addr, new RequesterEncoder(encoder), new ReplierDecoder<>(decoder));
    }

    /**
     * @param addr
     * @param encoder
     * @param replier
     * @param <T>
     * @return
     */
    public <T> CompletableFuture<T> execute(InetSocketAddress addr, Requester.Encoder encoder, Replier<T> replier) {
        return execute(addr, new RequesterEncoder(encoder), replier);
    }

    /**
     * @param addr
     * @param requester
     * @param replier
     * @param <T>
     * @return
     */
    public <T> CompletableFuture<T> execute(InetSocketAddress addr, Requester requester, Replier<T> replier) {
        CompletableFuture<T> promise = new CompletableFuture<>();
        execute(addr, requester, replier, promise);
        return promise;
    }

    private <T> void execute(InetSocketAddress addr, Requester requester, Replier<T> replier, CompletableFuture<T> promise) {
        FastdfsPool pool = poolGroup.get(addr);
        pool.acquire().addListener(new FastdfsChannelListener<>(pool, requester, replier, promise));
    }

    @PreDestroy
    public void close() throws IOException {
        if (null != poolGroup) {
            try {
                poolGroup.close();
            } catch (Exception e) {
                // ignore
            }
        }
        if (null != loopGroup) {
            loopGroup.shutdownGracefully();
        }
    }

    private static class FastdfsChannelListener<T> implements FutureListener<Channel> {

        final FastdfsPool pool;
        final Requester requester;
        final Replier<T> replier;
        final CompletableFuture<T> promise;

        FastdfsChannelListener(FastdfsPool pool,
                               Requester requester,
                               Replier<T> replier,
                               CompletableFuture<T> promise) {
            this.pool = pool;
            this.requester = requester;
            this.replier = replier;
            this.promise = promise;
        }

        @Override
        public void operationComplete(Future<Channel> cf) throws Exception {

            if (cf.isCancelled()) {
                promise.cancel(true);
                return;
            }

            if (!cf.isSuccess()) {
                promise.completeExceptionally(cf.cause());
                return;
            }

            Channel channel = cf.getNow();
            promise.whenComplete((result, error) -> pool.release(channel));

            try {

                FastdfsOperation<T> fastdfsOperation = new FastdfsOperation<>(channel, requester, replier, promise);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("execute {}", fastdfsOperation);
                }

                fastdfsOperation.execute();
            } catch (Exception e) {
                promise.completeExceptionally(e);
            }
        }
    }
}
