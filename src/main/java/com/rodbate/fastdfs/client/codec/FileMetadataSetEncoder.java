/**
 *
 */
package com.rodbate.fastdfs.client.codec;

import com.rodbate.fastdfs.client.common.FastdfsConstants;
import com.rodbate.fastdfs.client.common.FastdfsUtils;
import com.rodbate.fastdfs.client.exchange.Requester;
import com.rodbate.fastdfs.client.vo.FileId;
import com.rodbate.fastdfs.client.vo.FileMetadata;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.rodbate.fastdfs.client.common.FastdfsConstants.*;
import static com.rodbate.fastdfs.client.common.FastdfsConstants.Commands.*;
import static io.netty.util.CharsetUtil.UTF_8;

/**
 * 设置文件属性请求
 *
 *
 */
public class FileMetadataSetEncoder implements Requester.Encoder {

    private final FileId fileId;
    private final FileMetadata metadata;
    private final byte flag;

    /**
     * @param fileId
     * @param metadata
     * @param flag
     */
    public FileMetadataSetEncoder(FileId fileId, FileMetadata metadata, byte flag) {
        this.fileId = Objects.requireNonNull(fileId);
        this.metadata = metadata;
        this.flag = flag;
    }

    @Override
    public List<Object> encode(ByteBufAllocator alloc) {
        byte[] pathBytes = fileId.pathBytes();
        byte[] metadatas = metadata.toBytes(UTF_8);
        int length = 2 * FDFS_LONG_LEN + 1 + FDFS_GROUP_LEN + pathBytes.length + metadatas.length;
        byte cmd = METADATA_SET;

        ByteBuf buf = alloc.buffer(length + FDFS_HEAD_LEN);
        buf.writeLong(length);
        buf.writeByte(cmd);
        buf.writeByte(ERRNO_OK);

        buf.writeLong(pathBytes.length);
        buf.writeLong(metadatas.length);
        buf.writeByte(flag);
        FastdfsUtils.writeFixLength(buf, fileId.group(), FDFS_GROUP_LEN);
        buf.writeBytes(pathBytes);
        buf.writeBytes(metadatas);
        return Collections.singletonList(buf);
    }
}
