/**
 *
 */
package com.rodbate.fastdfs.client.exception;

/**
 * Fastdfs 异常
 *
 *
 */
public class FastdfsException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = -8274679139300220262L;

    public FastdfsException() {
    }

    public FastdfsException(String message, Throwable cause) {
        super(message, cause);
    }

    public FastdfsException(String message) {
        super(message);
    }

    public FastdfsException(Throwable cause) {
        super(cause);
    }

}
