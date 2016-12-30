package com.rodbate.fastdfs.client.codec;

import com.rodbate.fastdfs.client.common.FastdfsConstants;
import com.rodbate.fastdfs.client.common.FastdfsUtils;
import com.rodbate.fastdfs.client.exchange.Requester;
import com.rodbate.fastdfs.client.vo.FileId;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;

import java.util.Collections;
import java.util.List;

/**
 *
 */
public abstract class FileIdOperationEncoder implements Requester.Encoder {

    private final FileId fileId;

    /**
     * @param fileId
     */
    protected FileIdOperationEncoder(FileId fileId) {
        this.fileId = fileId;
    }

    @Override
    public List<Object> encode(ByteBufAllocator alloc) {
        byte cmd = cmd();
        int length = FastdfsConstants.FDFS_GROUP_LEN + fileId.pathBytes().length;
        ByteBuf buf = alloc.buffer(length + FastdfsConstants.FDFS_HEAD_LEN);
        buf.writeLong(length);
        buf.writeByte(cmd);
        buf.writeByte(FastdfsConstants.ERRNO_OK);
        FastdfsUtils.writeFixLength(buf, fileId.group(), FastdfsConstants.FDFS_GROUP_LEN);
        ByteBufUtil.writeUtf8(buf, fileId.path());
        return Collections.singletonList(buf);
    }

    /**
     * @return
     */
    protected abstract byte cmd();
}
