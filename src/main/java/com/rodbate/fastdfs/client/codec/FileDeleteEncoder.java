/**
 *
 */
package com.rodbate.fastdfs.client.codec;

import com.rodbate.fastdfs.client.common.FastdfsConstants;
import com.rodbate.fastdfs.client.vo.FileId;

/**
 * 删除请求
 *
 * @author liulongbiao
 */
public class FileDeleteEncoder extends FileIdOperationEncoder {

    public FileDeleteEncoder(FileId fileId) {
        super(fileId);
    }

    @Override
    protected byte cmd() {
        return FastdfsConstants.Commands.FILE_DELETE;
    }
}
