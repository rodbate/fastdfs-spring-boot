package com.rodbate.fastdfs.client.exception;





public class FastdfsNotFoundException extends RuntimeException {


    public FastdfsNotFoundException() {
        super("Fast DFS resource not found / exist");
    }

}
