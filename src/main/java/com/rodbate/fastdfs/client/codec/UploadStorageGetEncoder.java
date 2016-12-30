/**
 *
 */
package com.rodbate.fastdfs.client.codec;

import com.rodbate.fastdfs.client.common.FastdfsUtils;
import com.rodbate.fastdfs.client.exchange.Requester;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.util.Collections;
import java.util.List;

import static com.rodbate.fastdfs.client.common.FastdfsConstants.*;
import static com.rodbate.fastdfs.client.common.FastdfsConstants.Commands.*;
import static com.rodbate.fastdfs.client.common.FastdfsUtils.isEmpty;

/**
 * 获取可上传的存储服务器
 *
 *
 */
public class UploadStorageGetEncoder implements Requester.Encoder {

    private String group;

    public UploadStorageGetEncoder(String group) {
        this.group = group;
    }

    @Override
    public List<Object> encode(ByteBufAllocator alloc) {
        int length = isEmpty(group) ? 0 : FDFS_GROUP_LEN;
        byte cmd = isEmpty(group) ? SERVICE_QUERY_STORE_WITHOUT_GROUP_ONE : SERVICE_QUERY_STORE_WITH_GROUP_ONE;

        ByteBuf buf = alloc.buffer(length + FDFS_HEAD_LEN);
        buf.writeLong(length);
        buf.writeByte(cmd);
        buf.writeByte(ERRNO_OK);
        if (!isEmpty(group)) {
            FastdfsUtils.writeFixLength(buf, group, FDFS_GROUP_LEN);
        }
        return Collections.singletonList(buf);
    }
}
