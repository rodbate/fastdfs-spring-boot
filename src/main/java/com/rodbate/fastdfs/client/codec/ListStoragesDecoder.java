package com.rodbate.fastdfs.client.codec;


import com.rodbate.fastdfs.client.common.FastdfsConstants;
import com.rodbate.fastdfs.client.exchange.Replier;
import com.rodbate.fastdfs.client.vo.StorageStat;
import io.netty.buffer.ByteBuf;

import java.util.LinkedList;
import java.util.List;


public class ListStoragesDecoder implements Replier.Decoder<List<StorageStat>>{



    @Override
    public List<StorageStat> decode(ByteBuf buf) {

        List<StorageStat> list = new LinkedList<>();

        while (buf.isReadable()) {

            StorageStat stat = new StorageStat();

            stat.setStatus(buf.readByte());

            //storage id
            byte id[] = new byte[FastdfsConstants.FDFS_STORAGE_ID_MAX_SIZE];
            buf.readBytes(id);
            stat.setId(new String(id, FastdfsConstants.UTF_8));

            //ip address
            byte ip[] = new byte[FastdfsConstants.FDFS_IPADDR_SIZE];
            buf.readBytes(ip);
            stat.setIpAddr(new String(ip, FastdfsConstants.UTF_8));

            //domain
            byte domain[] = new byte[FastdfsConstants.FDFS_DOMAIN_NAME_MAX_SIZE];
            buf.readBytes(domain);
            stat.setDomainName(new String(domain, FastdfsConstants.UTF_8));

            //source ip address
            byte srcIp[] = new byte[FastdfsConstants.FDFS_IPADDR_SIZE];
            buf.readBytes(srcIp);
            stat.setSrcIpAddr(new String(srcIp, FastdfsConstants.UTF_8));

            //version
            byte version[] = new byte[FastdfsConstants.FDFS_VERSION_SIZE];
            buf.readBytes(version);
            stat.setVersion(new String(version, FastdfsConstants.UTF_8));

            //storage join time
            stat.setJoinTime(buf.readLong());

            //up time
            stat.setUpTime(buf.readLong());

            stat.setTotalMB(buf.readLong());
            stat.setFreeMB(buf.readLong());
            stat.setUploadPriority((int) buf.readLong());
            stat.setStorePathCount((int) buf.readLong());
            stat.setSubdirCountPerPath((int) buf.readLong());
            stat.setCurrentWritePath((int) buf.readLong());
            stat.setStoragePort((int) buf.readLong());
            stat.setStorageHttpPort((int) buf.readLong());
            stat.setConnectionAllocCount(buf.readInt());
            stat.setConnectionCurrentCount(buf.readInt());
            stat.setConnectionMaxCount(buf.readInt());

            stat.setTotalUploadCount(buf.readLong());
            stat.setSuccessUploadCount(buf.readLong());
            stat.setTotalAppendCount(buf.readLong());
            stat.setSuccessAppendCount(buf.readLong());
            stat.setTotalModifyCount(buf.readLong());
            stat.setSuccessModifyCount(buf.readLong());
            stat.setTotalTruncateCount(buf.readLong());
            stat.setSuccessTruncateCount(buf.readLong());
            stat.setTotalSetMetaCount(buf.readLong());
            stat.setSuccessSetMetaCount(buf.readLong());
            stat.setTotalDeleteCount(buf.readLong());
            stat.setSuccessDeleteCount(buf.readLong());
            stat.setTotalDownloadCount(buf.readLong());
            stat.setSuccessDownloadCount(buf.readLong());
            stat.setTotalGetMetaCount(buf.readLong());
            stat.setSuccessGetMetaCount(buf.readLong());
            stat.setTotalCreateLinkCount(buf.readLong());
            stat.setSuccessCreateLinkCount(buf.readLong());
            stat.setTotalDeleteLinkCount(buf.readLong());
            stat.setSuccessDeleteLinkCount(buf.readLong());

            stat.setTotalUploadBytes(buf.readLong());
            stat.setSuccessUploadBytes(buf.readLong());
            stat.setTotalAppendBytes(buf.readLong());
            stat.setSuccessAppendBytes(buf.readLong());
            stat.setTotalModifyBytes(buf.readLong());
            stat.setSuccessModifyBytes(buf.readLong());
            stat.setTotalDownloadloadBytes(buf.readLong());
            stat.setSuccessDownloadloadBytes(buf.readLong());
            stat.setTotalSyncInBytes(buf.readLong());
            stat.setSuccessSyncInBytes(buf.readLong());
            stat.setTotalSyncOutBytes(buf.readLong());
            stat.setSuccessSyncOutBytes(buf.readLong());

            stat.setTotalFileOpenCount(buf.readLong());
            stat.setSuccessFileOpenCount(buf.readLong());
            stat.setTotalFileReadCount(buf.readLong());
            stat.setSuccessFileReadCount(buf.readLong());
            stat.setTotalFileWriteCount(buf.readLong());
            stat.setSuccessFileWriteCount(buf.readLong());


            stat.setLastSourceUpdate(buf.readLong());
            stat.setLastSyncUpdate(buf.readLong());
            stat.setLastSyncedTimestamp(buf.readLong());
            stat.setLastHeartBeatTime(buf.readLong());

            stat.setIfTrunkServer(buf.readByte() != 0);

            list.add(stat);
        }


        return list;
    }
}
