package com.rodbate.fastdfs.client.common;




/**
 *
 *
 * fast dfs rpc响应码
 *
 */
public class FastDFSResponseCode {


    /**
     * success
     */
    public static final byte ERR_NO_OK = 0;

    /**
     * not found
     */
    public static final byte ERR_NO_ENOENT = 2;
    public static final byte ERR_NO_EIO = 5;

    /**
     * system busy
     */
    public static final byte ERR_NO_EBUSY = 16;

    /**
     * response invalid
     */
    public static final byte ERR_NO_EINVAL = 22;
    public static final byte ERR_NO_ENOSPC = 28;

    /**
     * connect refused
     */
    public static final byte ECONNREFUSED = 61;
    public static final byte ERR_NO_EALREADY = 114;


}
