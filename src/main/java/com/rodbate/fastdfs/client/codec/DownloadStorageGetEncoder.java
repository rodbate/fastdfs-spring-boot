/**
 *
 */
package com.rodbate.fastdfs.client.codec;

import com.rodbate.fastdfs.client.common.FastdfsConstants;
import com.rodbate.fastdfs.client.vo.FileId;

/**
 * 获取可下载的存储服务器
 *
 *
 */
public class DownloadStorageGetEncoder extends FileIdOperationEncoder {

    public DownloadStorageGetEncoder(FileId fileId) {
        super(fileId);
    }

    @Override
    protected byte cmd() {
        return FastdfsConstants.Commands.SERVICE_QUERY_FETCH_ONE;
    }

}
