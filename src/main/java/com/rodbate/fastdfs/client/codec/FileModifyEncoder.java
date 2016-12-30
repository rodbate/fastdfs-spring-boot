/**
 *
 */
package com.rodbate.fastdfs.client.codec;

import com.rodbate.fastdfs.client.common.FastdfsConstants;
import com.rodbate.fastdfs.client.vo.FileId;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.io.File;

/**
 * 修改文件请求
 *
 *
 */
public class FileModifyEncoder extends FileOperationEncoder {

    private final FileId fileId;
    private final long offset;

    public FileModifyEncoder(FileId fileId, File file, int offset) {
        super(file);
        this.fileId = fileId;
        this.offset = offset;
    }

    public FileModifyEncoder(FileId fileId, Object content, long size, long offset) {
        super(content, size);
        this.fileId = fileId;
        this.offset = offset;
    }

    @Override
    protected byte cmd() {
        return FastdfsConstants.Commands.FILE_MODIFY;
    }

    @Override
    protected ByteBuf metadata(ByteBufAllocator alloc) {
        byte[] pathBytes = fileId.pathBytes();
        int metaLen = 3 * FastdfsConstants.FDFS_PROTO_PKG_LEN_SIZE + pathBytes.length;
        ByteBuf buf = alloc.buffer(metaLen);
        buf.writeLong(pathBytes.length);
        buf.writeLong(offset);
        buf.writeLong(size());
        buf.writeBytes(pathBytes);
        return buf;
    }
}
