package com.rodbate.fastdfs.client.codec;

import com.rodbate.fastdfs.client.common.FastdfsUtils;
import com.rodbate.fastdfs.client.exchange.Replier;
import com.rodbate.fastdfs.client.vo.FileInfo;
import io.netty.buffer.ByteBuf;

/**
 *
 */
public enum FileInfoDecoder implements Replier.Decoder<FileInfo> {

    INSTANCE;

    @Override
    public FileInfo decode(ByteBuf buf) {
        long fileSize = buf.readLong();
        long createTime = buf.readLong();
        long crc32 = buf.readLong();
        String address = FastdfsUtils.readString(buf, 16);
        return FileInfo.newBuilder()
                        .fileSize(fileSize)
                        .createTime(createTime)
                        .crc32(crc32)
                        .address(address)
                        .build();
    }
}
