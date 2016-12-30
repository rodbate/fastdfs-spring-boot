/**
 *
 */
package com.rodbate.fastdfs.client;

import com.rodbate.fastdfs.client.codec.*;
import com.rodbate.fastdfs.client.common.FastdfsUtils;
import com.rodbate.fastdfs.client.executor.FastdfsExecutor;
import com.rodbate.fastdfs.client.vo.FileId;
import com.rodbate.fastdfs.client.vo.GroupStat;
import com.rodbate.fastdfs.client.vo.StorageStat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;


public final class TrackerClient {

    private static final Logger LOG = LoggerFactory.getLogger(TrackerClient.class);

    private final FastdfsExecutor executor;
    private final TrackerSelector selector;
    private final List<TrackerServer> servers;

    TrackerClient(FastdfsExecutor executor, TrackerSelector selector, List<TrackerServer> servers) {
        this.executor = executor;
        this.servers = Collections.unmodifiableList(servers);
        this.selector = servers.size() == 1 ? TrackerSelector.FIRST : selector;
        LOG.info("TrackerClient inited with {} servers and selector {}.", servers.size(), this.selector);
    }

    private InetSocketAddress trackerSelect() {
        return selector.select(servers).toInetAddress();
    }



    //list groups
    public CompletableFuture<List<GroupStat>> listGroups() {

        //
        return executor.execute(trackerSelect(), new ListGroupsEncoder(), new ListGroupsDecoder());
    }


    //list storages
    public CompletableFuture<List<StorageStat>> listStorages(String groupName, String storageIpAddress) {

        return executor.execute(trackerSelect(), new ListStoragesEncoder(groupName, storageIpAddress), new ListStoragesDecoder());
    }


    /**
     * @return
     */
    public CompletableFuture<StorageServer> uploadStorageGet() {
        return uploadStorageGet(null);
    }

    /**
     * @param group
     * @return
     */
    CompletableFuture<StorageServer> uploadStorageGet(String group) {
        return executor.execute(trackerSelect(), new UploadStorageGetEncoder(group), StorageServerDecoder.INSTANCE);
    }

    /**
     * @param fileId
     * @return
     */
    CompletableFuture<StorageServer> downloadStorageGet(FileId fileId) {
        CompletableFuture<List<StorageServer>> result = executor.execute(trackerSelect(), new DownloadStorageGetEncoder(fileId), StorageServerListDecoder.INSTANCE);
        return result.thenApply(FastdfsUtils::first);
    }

    /**
     * 获取更新存储服务器地址
     *
     * @param fileId
     */
    CompletableFuture<StorageServer> updateStorageGet(FileId fileId) {
        CompletableFuture<List<StorageServer>> result = executor.execute(trackerSelect(), new UpdateStorageGetEncoder(fileId), StorageServerListDecoder.INSTANCE);
        return result.thenApply(FastdfsUtils::first);
    }

    /**
     * @param fileId
     * @return
     */
    CompletableFuture<List<StorageServer>> downloadStorageList(FileId fileId) {
        return executor.execute(trackerSelect(), new DownloadStorageListEncoder(fileId), StorageServerListDecoder.INSTANCE);
    }
}
