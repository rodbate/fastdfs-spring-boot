/**
 *
 */
package com.rodbate.fastdfs.client;

import com.google.gson.Gson;
import com.rodbate.fastdfs.FDFSConfig;
import com.rodbate.fastdfs.client.executor.FastdfsExecutor;
import com.rodbate.fastdfs.client.executor.FastdfsSettings;
import com.rodbate.fastdfs.client.vo.FileId;
import com.rodbate.fastdfs.client.vo.FileInfo;
import com.rodbate.fastdfs.client.vo.FileMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


@Component
public final class FastdfsClient implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(FastdfsClient.class);

    private static final Gson gson = new Gson();

    public static final long DEFAULT_CONNECT_TIMEOUT = 3000;
    public static final long DEFAULT_READ_TIMEOUT = 3000;
    public static final long DEFAULT_IDLE_TIMEOUT = 60000;

    public static final int DEFAULT_MAX_THREADS = 8;
    public static final int DEFAULT_MAX_CONN_PER_HOST = 100;

    private FastdfsExecutor executor;
    private TrackerClient trackerClient;
    private StorageClient storageClient;


    @Autowired
    FDFSConfig config;


    public FastdfsClient() {
    }


    @PostConstruct
    public void init() {

        long readTimeout = config.getReadTimeout() <= 0 ? DEFAULT_READ_TIMEOUT : config.getReadTimeout();
        long connectTimeout = config.getConnectTimeout() <= 0 ? DEFAULT_CONNECT_TIMEOUT : config.getConnectTimeout();
        int threadNum = config.getThreadNum() <= 0 ? DEFAULT_MAX_THREADS : config.getThreadNum();
        int maxConnection = config.getMaxConnection() <= 0 ? DEFAULT_MAX_CONN_PER_HOST : config.getMaxConnection();
        long idleTimeout = config.getIdleTimeout() <= 0 ? DEFAULT_IDLE_TIMEOUT : config.getIdleTimeout();
        List<String> trackerServers = config.getTrackerServers() == null ? new ArrayList<>() : config.getTrackerServers();

        LOGGER.info("fast dfs client config : [readTimeout={}; connectTimeout={}; idleTimeout={}; threadNum={}; maxConnection={}; trackerServers={}]",
                readTimeout, connectTimeout, idleTimeout, threadNum, maxConnection, gson.toJson(trackerServers));

        FastdfsClient.Builder builder = FastdfsClient.newBuilder()
                .connectTimeout(connectTimeout)
                .readTimeout(readTimeout)
                .maxThreads(threadNum)
                .maxConnPerHost(maxConnection)
                .idleTimeout(idleTimeout);

        if (trackerServers == null || trackerServers.size() == 0) {
            LOGGER.warn("Application : fastdfsClient => tracker server list is null or empty!");
            System.exit(1);
        }

        for (String str : trackerServers) {
            try {
                builder.tracker(str.split(":")[0], Integer.valueOf(str.split(":")[1]));
            }catch (Exception e){
                LOGGER.warn("Application : fastdfsClient => tracker server address [127.0.0.1:22122]");
                System.exit(2);
            }
        }

        FastdfsSettings settings = new FastdfsSettings(
                builder.connectTimeout,
                builder.readTimeout,
                builder.idleTimeout,
                builder.maxThreads,
                builder.maxConnPerHost
        );

        this.executor = new FastdfsExecutor(settings);
        this.trackerClient = new TrackerClient(executor, builder.selector, builder.trackers);
        this.storageClient = new StorageClient(executor);
    }

    private FastdfsClient(Builder builder) {

        FastdfsSettings settings = new FastdfsSettings(
                builder.connectTimeout,
                builder.readTimeout,
                builder.idleTimeout,
                builder.maxThreads,
                builder.maxConnPerHost
        );

        this.executor = new FastdfsExecutor(settings);
        this.trackerClient = new TrackerClient(executor, builder.selector, builder.trackers);
        this.storageClient = new StorageClient(executor);
    }

    /**
     * @param file
     * @return
     */
    public CompletableFuture<FileId> upload(File file) {
        return upload(null, file);
    }


    /**
     * @param group
     * @param file
     * @return
     */
    public CompletableFuture<FileId> upload(String group, File file) {
        Objects.requireNonNull(file, "file must not be null.");
        return trackerClient
                .uploadStorageGet(group)
                .thenCompose(server -> storageClient.upload(server, file));
    }

    /**
     * @param filename
     * @param content
     * @return
     */
    public CompletableFuture<FileId> upload(String filename, byte[] content) {
        return upload(null, filename, content);
    }

    /**
     * @param filename
     * @param content
     * @param metadata
     * @return
     */
    public CompletableFuture<FileId> upload(String filename, byte[] content, FileMetadata metadata) {
        return upload(null, filename, content, metadata);
    }

    /**
     * @param group
     * @param filename
     * @param content
     * @return
     */
    public CompletableFuture<FileId> upload(String group, String filename, byte[] content) {
        return upload(group, content, filename, content.length);
    }

    /**
     * @param group
     * @param filename
     * @param content
     * @param metadata
     * @return
     */
    public CompletableFuture<FileId> upload(String group, String filename, byte[] content, FileMetadata metadata) {
        return upload(group, content, filename, content.length, metadata);
    }


    /**
     * @param file
     * @param metadata
     * @return
     */
    public CompletableFuture<FileId> upload(File file, FileMetadata metadata) {
        return upload(null, file, metadata);
    }

    /**
     * @param group
     * @param file
     * @param metadata
     * @return
     */
    public CompletableFuture<FileId> upload(String group, File file, FileMetadata metadata) {
        Objects.requireNonNull(file, "file must not be null.");
        Objects.requireNonNull(metadata, "metadata must not be null.");
        return upload(group, file).thenApply(fileId -> {
            metadataSet(fileId, metadata);
            return fileId;
        });
    }

    /**
     * @param content
     * @param filename
     * @param size
     * @return
     */
    public CompletableFuture<FileId> upload(Object content, String filename, long size) {
        return upload(null, content, filename, size);
    }

    /**
     * 上传文件，其中文件内容字段 content 的支持以下类型：
     * <p>
     * <ul>
     * <li><code>byte[]</code></li>
     * <li>{@link File}</li>
     * <li>{@link java.io.InputStream}</li>
     * <li>{@link java.nio.channels.ReadableByteChannel}</li>
     * </ul>
     *
     * @param group    分组
     * @param content  上传内容
     * @param size     内容长度
     * @param filename 扩展名
     * @return
     */
    public CompletableFuture<FileId> upload(String group, Object content, String filename, long size) {
        Objects.requireNonNull(content, "content must not be null.");
        Objects.requireNonNull(filename, "filename must not be null.");
        return trackerClient
                .uploadStorageGet(group)
                .thenCompose(server -> storageClient.upload(server, content, filename, size));
    }

    /**
     * @param content
     * @param filename
     * @param size
     * @param metadata
     * @return
     */
    public CompletableFuture<FileId> upload(Object content, String filename, long size, FileMetadata metadata) {
        return upload(null, content, filename, size, metadata);
    }

    /**
     * @param group
     * @param content
     * @param filename
     * @param size
     * @param metadata
     * @return
     */
    public CompletableFuture<FileId> upload(String group, Object content, String filename, long size, FileMetadata metadata) {
        Objects.requireNonNull(content, "content must not be null.");
        Objects.requireNonNull(filename, "filename must not be null.");
        Objects.requireNonNull(metadata, "metadata must not be null.");
        return upload(group, content, filename, size).thenApply(fileId -> {
            metadataSet(fileId, metadata);
            return fileId;
        });
    }

    /**
     * @param file
     * @return
     */
    public CompletableFuture<FileId> uploadAppender(File file) {
        return uploadAppender(null, file);
    }

    /**
     * @param file
     * @param metadata
     * @return
     */
    public CompletableFuture<FileId> uploadAppender(File file, FileMetadata metadata) {
        return uploadAppender(null, file, metadata);
    }

    /**
     * @param group
     * @param file
     * @return
     */
    public CompletableFuture<FileId> uploadAppender(String group, File file) {
        Objects.requireNonNull(file, "file must not be null.");
        return trackerClient
                .uploadStorageGet(group)
                .thenCompose(server -> storageClient.uploadAppender(server, file));
    }

    /**
     * @param group
     * @param file
     * @return
     */
    public CompletableFuture<FileId> uploadAppender(String group, File file, FileMetadata metadata) {
        return uploadAppender(group, file)
                .thenApply(fileId -> {
                    metadataSet(fileId, metadata);
                    return fileId;
                });
    }

    /**
     * @param filename
     * @param content
     * @return
     */
    public CompletableFuture<FileId> uploadAppender(String filename, byte[] content) {
        return uploadAppender(null, content, filename, content.length);
    }

    /**
     * @param group
     * @param filename
     * @param content
     * @return
     */
    public CompletableFuture<FileId> uploadAppender(String group, String filename, byte[] content) {
        return uploadAppender(group, content, filename, content.length);
    }

    /**
     * @param group
     * @param filename
     * @param content
     * @param metadata
     * @return
     */
    public CompletableFuture<FileId> uploadAppender(String group, String filename, byte[] content, FileMetadata metadata) {
        return uploadAppender(group, content, filename, content.length, metadata);
    }

    /**
     * @param content
     * @param filename
     * @param size
     * @return
     */
    public CompletableFuture<FileId> uploadAppender(Object content, String filename, long size) {
        return uploadAppender(null, content, filename, size);
    }


    /**
     * @param content
     * @param filename
     * @param size
     * @param metadata
     * @return
     */
    public CompletableFuture<FileId> uploadAppender(Object content, String filename, long size, FileMetadata metadata) {
        return uploadAppender(null, content, filename, size, metadata);
    }

    /**
     * 上传可追加文件，其中文件内容字段 content 的支持以下类型：
     * <p>
     * <ul>
     * <li><code>byte[]</code></li>
     * <li>{@link File}</li>
     * <li>{@link java.io.InputStream}</li>
     * <li>{@link java.nio.channels.ReadableByteChannel}</li>
     * </ul>
     *
     * @param group    分组
     * @param content  上传内容
     * @param filename 文件名
     * @param size     内容长度
     * @return
     */
    public CompletableFuture<FileId> uploadAppender(String group, Object content, String filename, long size) {
        Objects.requireNonNull(content, "content must not be null.");
        Objects.requireNonNull(filename, "filename must not be null.");
        return trackerClient
                .uploadStorageGet(group)
                .thenCompose(server -> storageClient.uploadAppender(server, content, filename, size));
    }

    /**
     * @param group
     * @param content
     * @param filename
     * @param size
     * @param metadata
     * @return
     */
    public CompletableFuture<FileId> uploadAppender(String group, Object content, String filename, long size, FileMetadata metadata) {
        return uploadAppender(group, content, filename, size)
                .thenApply(fileId -> {
                    metadataSet(fileId, metadata);
                    return fileId;
                });
    }

    /**
     * @param fileId
     * @param out
     * @return
     */
    public CompletableFuture<Long> download(String fileId, Object out) {
        return download(FileId.fromString(fileId), out);
    }

    /**
     * 下载文件，其输出 output 参数支持以下类型
     * <p>
     * <ul>
     * <li>{@link java.io.OutputStream}</li>
     * <li>{@link java.nio.channels.GatheringByteChannel}</li>
     * </ul>
     *
     * @param fileId 服务器存储路径
     * @param out    输出流
     * @return
     */
    public CompletableFuture<Long> download(FileId fileId, Object out) {
        Objects.requireNonNull(fileId, "fileId must not be null.");
        Objects.requireNonNull(out, "out must not be null.");
        return trackerClient
                .downloadStorageGet(fileId)
                .thenCompose(server -> storageClient.download(server, fileId, out));
    }

    /**
     * @param fileId
     * @param out
     * @param offset
     * @param size
     * @return
     */
    public CompletableFuture<Long> download(String fileId, Object out, int offset, int size) {
        return download(FileId.fromString(fileId), out, offset, size);
    }

    /**
     * @param fileId
     * @param out
     * @param offset
     * @param size
     * @return
     */
    public CompletableFuture<Long> download(FileId fileId, Object out, int offset, int size) {
        Objects.requireNonNull(fileId, "fileId must not be null.");
        Objects.requireNonNull(out, "out must not be null.");
        return trackerClient
                .downloadStorageGet(fileId)
                .thenCompose(server -> storageClient.download(server, fileId, out, offset, size));
    }

    /**
     * @param fileId
     * @return
     */
    public CompletableFuture<Void> delete(String fileId) {
        return delete(FileId.fromString(fileId));
    }

    /**
     * @param fileId
     * @return
     */
    public CompletableFuture<Void> delete(FileId fileId) {
        Objects.requireNonNull(fileId, "fileId must not be null.");
        return trackerClient
                .updateStorageGet(fileId)
                .thenCompose(server -> storageClient.delete(server, fileId));
    }

    /**
     * @param fileId
     * @param file
     * @return
     */
    public CompletableFuture<Void> append(String fileId, File file) {
        return append(FileId.fromString(fileId), file);
    }

    /**
     * 追加文件
     *
     * @param fileId 服务器存储路径
     * @param file   内容
     * @return
     */
    public CompletableFuture<Void> append(FileId fileId, File file) {
        Objects.requireNonNull(fileId, "fileId must not be null.");
        Objects.requireNonNull(file, "file must not be null.");
        return trackerClient
                .updateStorageGet(fileId)
                .thenCompose(server -> storageClient.append(server, fileId, file));
    }

    /**
     * @param fileId
     * @param bytes
     * @return
     */
    public CompletableFuture<Void> append(String fileId, byte[] bytes) {
        return append(FileId.fromString(fileId), bytes);
    }

    /**
     * 追加文件
     *
     * @param fileId 服务器存储路径
     * @param bytes  内容
     * @return
     */
    public CompletableFuture<Void> append(FileId fileId, byte[] bytes) {
        return append(fileId, bytes, bytes.length);
    }

    /**
     * @param fileId
     * @param content
     * @param size
     * @return
     */
    public CompletableFuture<Void> append(String fileId, Object content, long size) {
        return append(FileId.fromString(fileId), content, size);
    }

    /**
     * @param fileId
     * @param content
     * @param size
     * @return
     */
    public CompletableFuture<Void> append(FileId fileId, Object content, long size) {
        Objects.requireNonNull(fileId, "fileId must not be null.");
        Objects.requireNonNull(content, "content must not be null.");
        return trackerClient
                .updateStorageGet(fileId)
                .thenCompose(server -> storageClient.append(server, fileId, content, size));
    }

    /**
     * @param fileId
     * @param file
     * @param offset
     * @return
     */
    public CompletableFuture<Void> modify(String fileId, File file, int offset) {
        return modify(FileId.fromString(fileId), file, offset);
    }

    /**
     * @param fileId
     * @param file
     * @param offset
     * @return
     */
    public CompletableFuture<Void> modify(FileId fileId, File file, int offset) {
        Objects.requireNonNull(fileId, "fileId must not be null.");
        Objects.requireNonNull(file, "file must not be null.");
        return trackerClient
                .updateStorageGet(fileId)
                .thenCompose(server -> storageClient.modify(server, fileId, file, offset));
    }

    /**
     * @param fileId
     * @param bytes
     * @param offset
     * @return
     */
    public CompletableFuture<Void> modify(String fileId, byte[] bytes, int offset) {
        return modify(FileId.fromString(fileId), bytes, offset);
    }

    /**
     * @param fileId
     * @param bytes
     * @param offset
     * @return
     */
    public CompletableFuture<Void> modify(FileId fileId, byte[] bytes, int offset) {
        return modify(fileId, bytes, bytes.length, offset);
    }

    /**
     * @param fileId
     * @param content
     * @param size
     * @param offset
     * @return
     */
    public CompletableFuture<Void> modify(String fileId, Object content, long size, int offset) {
        return modify(FileId.fromString(fileId), content, size, offset);
    }

    /**
     * @param fileId
     * @param content
     * @param size
     * @param offset
     * @return
     */
    public CompletableFuture<Void> modify(FileId fileId, Object content, long size, long offset) {
        Objects.requireNonNull(fileId, "fileId must not be null.");
        Objects.requireNonNull(content, "content must not be null.");
        return trackerClient
                .updateStorageGet(fileId)
                .thenCompose(server -> storageClient.modify(server, fileId, content, size, offset));
    }

    /**
     * @param fileId
     * @return
     */
    public CompletableFuture<Void> truncate(String fileId) {
        return truncate(FileId.fromString(fileId));
    }

    /**
     * 截取文件
     *
     * @param fileId 服务器存储路径
     * @return
     */
    public CompletableFuture<Void> truncate(FileId fileId) {
        return truncate(fileId, 0);
    }

    /**
     * @param fileId
     * @param truncatedSize
     * @return
     */
    public CompletableFuture<Void> truncate(String fileId, long truncatedSize) {
        return truncate(FileId.fromString(fileId), truncatedSize);
    }

    /**
     * 截取文件
     *
     * @param fileId        服务器存储路径
     * @param truncatedSize 截取字节数
     * @return
     */
    public CompletableFuture<Void> truncate(FileId fileId, long truncatedSize) {
        Objects.requireNonNull(fileId, "fileId must not be null.");
        return trackerClient
                .updateStorageGet(fileId)
                .thenCompose(server -> storageClient.truncate(server, fileId, truncatedSize));
    }

    /**
     * @param fileId
     * @param metadata
     * @return
     */
    public CompletableFuture<Void> metadataSet(String fileId, FileMetadata metadata) {
        return metadataSet(FileId.fromString(fileId), metadata);
    }

    /**
     * 设置文件元数据
     *
     * @param fileId   服务器存储路径
     * @param metadata 元数据
     */
    public CompletableFuture<Void> metadataSet(FileId fileId, FileMetadata metadata) {
        return metadataSet(fileId, metadata, FileMetadata.OVERWRITE_FLAG);
    }

    /**
     * @param fileId
     * @param metadata
     * @param flag
     * @return
     */
    public CompletableFuture<Void> metadataSet(String fileId, FileMetadata metadata, byte flag) {
        return metadataSet(FileId.fromString(fileId), metadata, flag);
    }

    /**
     * 设置文件元数据
     *
     * @param fileId   服务器存储路径
     * @param metadata 元数据
     * @param flag     设置标识
     */
    public CompletableFuture<Void> metadataSet(FileId fileId, FileMetadata metadata, byte flag) {
        Objects.requireNonNull(fileId, "fileId must not be null.");
        Objects.requireNonNull(metadata, "metadata must not be null.");
        return trackerClient
                .updateStorageGet(fileId)
                .thenCompose(server -> storageClient.setMetadata(server, fileId, metadata, flag));
    }

    /**
     * @param fileId
     * @return
     */
    public CompletableFuture<FileMetadata> metadataGet(String fileId) {
        return metadataGet(FileId.fromString(fileId));
    }

    /**
     * 获取文件元数据
     *
     * @param fileId
     * @return
     */
    public CompletableFuture<FileMetadata> metadataGet(FileId fileId) {
        Objects.requireNonNull(fileId, "fileId must not be null.");
        return trackerClient
                .updateStorageGet(fileId)
                .thenCompose(server -> storageClient.getMetadata(server, fileId));
    }

    /**
     * @param fileId
     * @return
     */
    public CompletableFuture<FileInfo> infoGet(String fileId) {
        return infoGet(FileId.fromString(fileId));
    }

    /**
     * 获取文件信息
     *
     * @param fileId
     * @return
     */
    public CompletableFuture<FileInfo> infoGet(FileId fileId) {
        Objects.requireNonNull(fileId, "fileId must not be null.");
        return trackerClient
                .updateStorageGet(fileId)
                .thenCompose(server -> storageClient.getInfo(server, fileId));
    }

    @Override
    public void close() throws IOException {
        executor.close();
    }

    /**
     * @return
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        long connectTimeout = DEFAULT_CONNECT_TIMEOUT; // 连接超时时间(毫秒)
        long readTimeout = DEFAULT_READ_TIMEOUT;// 读超时时间(毫秒)
        long idleTimeout = DEFAULT_IDLE_TIMEOUT;// 连接闲置时间(毫秒)

        int maxThreads = DEFAULT_MAX_THREADS; // 线程数量
        int maxConnPerHost = DEFAULT_MAX_CONN_PER_HOST; // 每个IP最大连接数

        TrackerSelector selector = TrackerSelector.RANDOM;
        List<TrackerServer> trackers = new LinkedList<>();

        Builder() {
        }

        public Builder connectTimeout(long connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder readTimeout(long readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public Builder idleTimeout(long idleTimeout) {
            this.idleTimeout = idleTimeout;
            return this;
        }

        public Builder maxThreads(int maxThreads) {
            this.maxThreads = maxThreads;
            return this;
        }

        public Builder maxConnPerHost(int maxConnPerHost) {
            this.maxConnPerHost = maxConnPerHost;
            return this;
        }

        @Deprecated
        public Builder maxIdleSeconds(int maxIdleSeconds) {
            this.idleTimeout = TimeUnit.SECONDS.toMillis(maxIdleSeconds);
            return this;
        }

        /**
         * @param selector
         * @return
         */
        public Builder selector(TrackerSelector selector) {
            this.selector = Objects.requireNonNull(selector, "selector must not be null.");
            return this;
        }

        /**
         * @param servers
         * @return
         */
        public Builder trackers(List<TrackerServer> servers) {
            this.trackers = new LinkedList<>(Objects.requireNonNull(servers, "servers must not be null."));
            return this;
        }

        /**
         * @param server
         * @return
         */
        public Builder tracker(TrackerServer server) {
            this.trackers.add(Objects.requireNonNull(server, "server must not be null."));
            return this;
        }

        /**
         * @param host
         * @param port
         * @return
         */
        public Builder tracker(String host, int port) {
            return tracker(new TrackerServer(host, port));
        }

        /**
         * @param host
         * @param port
         * @param weight
         * @return
         */
        public Builder tracker(String host, int port, int weight) {
            return tracker(new TrackerServer(host, port, weight));
        }

        public FastdfsClient build() {
            return new FastdfsClient(this);
        }
    }


    public TrackerClient getTrackerClient() {
        return trackerClient;
    }

    public StorageClient getStorageClient() {
        return storageClient;
    }
}
