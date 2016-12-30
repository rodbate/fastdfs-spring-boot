/**
 *
 */
package com.rodbate.fastdfs.client.codec;


import com.rodbate.fastdfs.client.common.FastdfsConstants;
import com.rodbate.fastdfs.client.vo.FileId;

/**
 * 获取可下载的存储服务器列表
 *
 *
 */
public class DownloadStorageListEncoder extends FileIdOperationEncoder {

    public DownloadStorageListEncoder(FileId fileId) {
        super(fileId);
    }

    @Override
    protected byte cmd() {
        return FastdfsConstants.Commands.SERVICE_QUERY_FETCH_ALL;
    }

}
