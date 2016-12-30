/**
 *
 */
package com.rodbate.fastdfs.client.codec;

import com.rodbate.fastdfs.client.common.FastdfsConstants;
import com.rodbate.fastdfs.client.exchange.Requester;
import com.rodbate.fastdfs.client.vo.FileId;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.util.Collections;
import java.util.List;

import static io.netty.util.CharsetUtil.UTF_8;

/**
 * 截取请求
 *
 *
 */
public class FileTruncateEncoder implements Requester.Encoder {

    private final FileId fileId;
    private final long truncatedSize;

    public FileTruncateEncoder(FileId fileId, long truncatedSize) {
        this.fileId = fileId;
        this.truncatedSize = truncatedSize;
    }

    @Override
    public List<Object> encode(ByteBufAllocator alloc) {
        byte[] pathBytes = fileId.path().getBytes(UTF_8);
        int length = 2 * FastdfsConstants.FDFS_PROTO_PKG_LEN_SIZE + pathBytes.length;
        byte cmd = FastdfsConstants.Commands.FILE_TRUNCATE;

        ByteBuf buf = alloc.buffer(length + FastdfsConstants.FDFS_HEAD_LEN);
        buf.writeLong(length);
        buf.writeByte(cmd);
        buf.writeByte(FastdfsConstants.ERRNO_OK);
        buf.writeLong(pathBytes.length);
        buf.writeLong(truncatedSize);
        buf.writeBytes(pathBytes);
        return Collections.singletonList(buf);
    }

}
