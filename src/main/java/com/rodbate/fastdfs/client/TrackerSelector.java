package com.rodbate.fastdfs.client;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;


public enum TrackerSelector {

    ROUND_ROBIN {
        private AtomicInteger index = new AtomicInteger(0);

        @Override
        public TrackerServer select(List<TrackerServer> list) {
            int idx = Math.abs(index.getAndIncrement());
            return list.get(idx % list.size());
        }

    },
    RANDOM {
        private final Random random = new Random();

        @Override
        public TrackerServer select(List<TrackerServer> list) {
            return list.get(random.nextInt(list.size()));
        }

    },
    FIRST {
        @Override
        TrackerServer select(List<TrackerServer> list) {
            return list.get(0);
        }

    };

    abstract TrackerServer select(List<TrackerServer> list);
}