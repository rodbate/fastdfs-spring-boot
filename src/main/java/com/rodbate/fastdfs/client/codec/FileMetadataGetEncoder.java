/**
 *
 */
package com.rodbate.fastdfs.client.codec;

import com.rodbate.fastdfs.client.common.FastdfsConstants;
import com.rodbate.fastdfs.client.vo.FileId;

/**
 * 获取文件属性请求
 *
 *
 */
public class FileMetadataGetEncoder extends FileIdOperationEncoder {

    public FileMetadataGetEncoder(FileId fileId) {
        super(fileId);
    }

    @Override
    public byte cmd() {
        return FastdfsConstants.Commands.METADATA_GET;
    }

}