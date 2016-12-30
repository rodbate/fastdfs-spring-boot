package com.rodbate.fastdfs.client.codec;


import com.rodbate.fastdfs.client.exception.FastdfsException;
import com.rodbate.fastdfs.client.exchange.Requester;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.rodbate.fastdfs.client.common.FastdfsConstants.*;
import static com.rodbate.fastdfs.client.common.FastdfsConstants.Commands.*;
import static com.rodbate.fastdfs.client.common.FastdfsConstants.Commands.SERVER_LIST_STORAGE;

public class ListStoragesEncoder implements Requester.Encoder {


    private final String groupName;

    private final String storageIp;

    public ListStoragesEncoder(String groupName, String storageIp) {
        this.groupName = groupName;
        this.storageIp = storageIp;
    }

    @Override
    public List<Object> encode(ByteBufAllocator alloc) {

        byte group[] = new byte[FDFS_GROUP_LEN];
        Arrays.fill(group, (byte) 0);


        if (groupName != null && groupName.trim().length() > 0)
        {
            byte[] gn = groupName.getBytes(UTF_8);
            int groupNameLen = gn.length > FDFS_GROUP_LEN ? FDFS_GROUP_LEN : gn.length;
            System.arraycopy(gn, 0, group, 0, groupNameLen);
        }
        else
        {
            throw new FastdfsException("group name require not null");
        }

        byte storageIpBytes[];
        int ipLen;

        if (storageIp != null && storageIp.trim().length() > 0) {
            storageIpBytes = storageIp.getBytes(UTF_8);
            ipLen = storageIpBytes.length;
        }
        else {
            storageIpBytes = null;
            ipLen = 0;
        }

        ByteBuf buf = alloc.buffer(10 + FDFS_GROUP_LEN + ipLen);

        //write header
        buf.writeLong(FDFS_GROUP_LEN + ipLen);
        buf.writeByte(SERVER_LIST_STORAGE);
        buf.writeByte(ERRNO_OK);

        //write body
        buf.writeBytes(group);
        if (ipLen > 0){
            buf.writeBytes(storageIpBytes);
        }

        return Collections.singletonList(buf);
    }
}
