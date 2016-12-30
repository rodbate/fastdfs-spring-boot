package com.rodbate.fastdfs;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@ConfigurationProperties(prefix = "fdfs")
public class FDFSConfig {


    private long readTimeout;

    private long connectTimeout;

    private int threadNum;

    private int maxConnection;

    private long idleTimeout;

    private List<String> trackerServers;


    public long getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(long readTimeout) {
        this.readTimeout = readTimeout;
    }

    public long getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(long connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getThreadNum() {
        return threadNum;
    }

    public void setThreadNum(int threadNum) {
        this.threadNum = threadNum;
    }

    public int getMaxConnection() {
        return maxConnection;
    }

    public void setMaxConnection(int maxConnection) {
        this.maxConnection = maxConnection;
    }

    public List<String> getTrackerServers() {
        return trackerServers;
    }

    public void setTrackerServers(List<String> trackerServers) {
        this.trackerServers = trackerServers;
    }

    public long getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(long idleTimeout) {
        this.idleTimeout = idleTimeout;
    }
}
