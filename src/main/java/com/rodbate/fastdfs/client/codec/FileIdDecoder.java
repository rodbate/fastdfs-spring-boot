/**
 *
 */
package com.rodbate.fastdfs.client.codec;

import com.rodbate.fastdfs.client.exception.FastdfsException;
import com.rodbate.fastdfs.client.exchange.Replier;
import com.rodbate.fastdfs.client.vo.FileId;
import io.netty.buffer.ByteBuf;

import static com.rodbate.fastdfs.client.common.FastdfsConstants.FDFS_GROUP_LEN;
import static com.rodbate.fastdfs.client.common.FastdfsUtils.readString;

/**
 * 存储路径解码器
 *
 *
 */
public enum FileIdDecoder implements Replier.Decoder<FileId> {

    INSTANCE;

    @Override
    public FileId decode(ByteBuf in) {
        int length = in.readableBytes();
        if (length <= FDFS_GROUP_LEN) {
            throw new FastdfsException("body length : " + length + ", is lte required group name length 16.");
        }
        String group = readString(in, FDFS_GROUP_LEN);
        String path = readString(in);
        return new FileId(group, path);
    }

}