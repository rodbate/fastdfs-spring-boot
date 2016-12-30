package com.rodbate.fastdfs.client.codec;

import com.rodbate.fastdfs.client.common.FastdfsConstants;
import com.rodbate.fastdfs.client.vo.FileId;

/**
 *
 */
public class FileInfoGetEncoder extends FileIdOperationEncoder {

    /**
     * @param fileId
     */
    public FileInfoGetEncoder(FileId fileId) {
        super(fileId);
    }

    @Override
    protected byte cmd() {
        return FastdfsConstants.Commands.FILE_QUERY;
    }
}
