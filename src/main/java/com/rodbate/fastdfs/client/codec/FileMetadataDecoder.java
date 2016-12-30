/**
 *
 */
package com.rodbate.fastdfs.client.codec;

import com.rodbate.fastdfs.client.common.FastdfsConstants;
import com.rodbate.fastdfs.client.common.FastdfsUtils;
import com.rodbate.fastdfs.client.exchange.Replier;
import com.rodbate.fastdfs.client.vo.FileMetadata;
import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

import static com.rodbate.fastdfs.client.common.FastdfsUtils.readString;

/**
 * 文件属性解码器
 *
 *
 */
public enum FileMetadataDecoder implements Replier.Decoder<FileMetadata> {
    INSTANCE;

    @Override
    public FileMetadata decode(ByteBuf buf) {
        String content = FastdfsUtils.readString(buf);

        Map<String, String> values = new HashMap<>();
        String[] pairs = content.split(FastdfsConstants.FDFS_RECORD_SEPERATOR);
        for (String pair : pairs) {
            String[] kv = pair.split(FastdfsConstants.FDFS_FIELD_SEPERATOR, 2);
            if (kv.length == 2) {
                values.put(kv[0], kv[1]);
            }
        }
        return new FileMetadata(values);
    }

}
