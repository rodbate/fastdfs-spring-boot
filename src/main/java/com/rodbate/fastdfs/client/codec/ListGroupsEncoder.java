package com.rodbate.fastdfs.client.codec;


import com.rodbate.fastdfs.client.common.FastdfsConstants;
import com.rodbate.fastdfs.client.exchange.Requester;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.util.Collections;
import java.util.List;

/**
 *
 *
 *
 * list groups encoder
 */

public class ListGroupsEncoder implements Requester.Encoder{


    @Override
    public List<Object> encode(ByteBufAllocator alloc) {

        ByteBuf buffer = alloc.buffer(10);

        //body len 0 long
        buffer.writeLong(0L);

        //cmd
        buffer.writeByte(FastdfsConstants.Commands.SERVER_LIST_GROUP);

        //code
        buffer.writeByte(FastdfsConstants.ERRNO_OK);

        return Collections.singletonList(buffer);
    }
}
