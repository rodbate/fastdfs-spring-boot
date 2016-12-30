/**
 *
 */
package com.rodbate.fastdfs.client.codec;

import com.rodbate.fastdfs.client.StorageServer;
import com.rodbate.fastdfs.client.common.FastdfsConstants;
import com.rodbate.fastdfs.client.common.FastdfsUtils;
import com.rodbate.fastdfs.client.exchange.Replier;
import io.netty.buffer.ByteBuf;

import static com.rodbate.fastdfs.client.common.FastdfsUtils.readString;

/**
 * 存储服务器信息解码器
 *
 *
 */
public enum StorageServerDecoder implements Replier.Decoder<StorageServer> {

    INSTANCE;

    @Override
    public long expectLength() {
        return FastdfsConstants.FDFS_STORAGE_STORE_LEN;
    }

    @Override
    public StorageServer decode(ByteBuf in) {
        String group = readString(in, FastdfsConstants.FDFS_GROUP_LEN);
        String host = readString(in, FastdfsConstants.FDFS_HOST_LEN);
        int port = (int) in.readLong();
        byte idx = in.readByte();
        return new StorageServer(group, host, port, idx);
    }

}
