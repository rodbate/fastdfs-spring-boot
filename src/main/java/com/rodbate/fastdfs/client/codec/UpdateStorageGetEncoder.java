/**
 *
 */
package com.rodbate.fastdfs.client.codec;

import com.rodbate.fastdfs.client.vo.FileId;

import static com.rodbate.fastdfs.client.common.FastdfsConstants.Commands.SERVICE_QUERY_UPDATE;

/**
 * 获取可更新的存储服务器
 *
 *
 */
public class UpdateStorageGetEncoder extends FileIdOperationEncoder {

    public UpdateStorageGetEncoder(FileId fileId) {
        super(fileId);
    }

    @Override
    protected byte cmd() {
        return SERVICE_QUERY_UPDATE;
    }

}
