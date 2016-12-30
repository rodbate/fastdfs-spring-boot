/**
 *
 */
package com.rodbate.fastdfs.client.vo;


import com.rodbate.fastdfs.client.common.FastdfsConstants;

import java.util.Base64;
import java.util.Objects;

/**
 *
 */
public class FileId {

    private static final char SEPARATER = '/';

    private final String group;
    private final String path;


    public FileId(String group, String path) {
        this.group = Objects.requireNonNull(group, "group must not be null.");
        this.path = Objects.requireNonNull(path, "path must not be null.");
    }


    public String group() {
        return group;
    }


    public byte[] groupBytes() {
        return group.getBytes(FastdfsConstants.UTF_8);
    }


    public String path() {
        return path;
    }


    public byte[] pathBytes() {
        return path.getBytes(FastdfsConstants.UTF_8);
    }


    public byte[] toBytes() {
        return toString().getBytes(FastdfsConstants.UTF_8);
    }


    @Override
    public String toString() {
        return group + SEPARATER + path;
    }


    public String toBase64String() {
        return Base64.getUrlEncoder().encodeToString(toBytes());
    }

    /**
     * 从全路径构造存储路径
     *
     * @param fullPath
     * @return
     */
    public static FileId fromString(String fullPath) {
        if (fullPath == null || fullPath.length() == 0) {
            throw new IllegalArgumentException("fullPath should not be empty.");
        }
        int idx = fullPath.indexOf(SEPARATER);
        if (idx < 0) {
            throw new IllegalArgumentException("fullPath cannot find path separater.");
        }

        String group = fullPath.substring(0, idx);
        String path = fullPath.substring(idx + 1);
        return new FileId(group, path);
    }

    /**
     * @param base64
     * @return
     */
    public static FileId fromBase64String(String base64) {
        byte[] bytes = Base64.getUrlDecoder().decode(base64.getBytes(FastdfsConstants.UTF_8));
        return fromString(new String(bytes));
    }
}
