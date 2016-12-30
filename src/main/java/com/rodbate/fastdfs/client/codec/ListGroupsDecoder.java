package com.rodbate.fastdfs.client.codec;


import com.rodbate.fastdfs.client.exchange.Replier;
import com.rodbate.fastdfs.client.vo.GroupStat;
import io.netty.buffer.ByteBuf;

import java.util.LinkedList;
import java.util.List;

import static com.rodbate.fastdfs.client.common.FastdfsConstants.*;
import static com.rodbate.fastdfs.client.common.FastdfsConstants.Commands.*;

/**
 *
 *
 * list groups
 */

public class ListGroupsDecoder implements Replier.Decoder<List<GroupStat>>{


    @Override
    public List<GroupStat> decode(ByteBuf buf) {

        List<GroupStat> list = new LinkedList<>();

        while (buf.isReadable()){

            GroupStat stat = new GroupStat();

            byte group[] = new byte[FDFS_GROUP_LEN + 1];

            //group name
            buf.readBytes(group);
            stat.setGroupName(new String(group, UTF_8));

            //total size mb
            stat.setTotalMB(buf.readLong());
            stat.setFreeMB(buf.readLong());
            stat.setTrunkFreeMB(buf.readLong());
            stat.setStorageCount((int) buf.readLong());
            stat.setStoragePort((int) buf.readLong());
            stat.setStorageHttpPort((int) buf.readLong());
            stat.setActiveCount((int) buf.readLong());
            stat.setCurrentWriteServer((int) buf.readLong());
            stat.setStorePathCount((int) buf.readLong());
            stat.setSubdirCountPerPath((int) buf.readLong());
            stat.setCurrentTrunkFileId((int) buf.readLong());

            list.add(stat);
        }

        return list;
    }
}
