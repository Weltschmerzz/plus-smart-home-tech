package ru.yandex.practicum.analyzer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "analyzer.kafka")
public class AnalyzerKafkaProperties {

    private Topics topics = new Topics();
    private Consumer consumer = new Consumer();

    public Topics getTopics() {
        return topics;
    }

    public void setTopics(Topics topics) {
        this.topics = topics;
    }

    public Consumer getConsumer() {
        return consumer;
    }

    public void setConsumer(Consumer consumer) {
        this.consumer = consumer;
    }

    public static class Topics {
        private String hubs;
        private String snapshots;

        public String getHubs() {
            return hubs;
        }

        public void setHubs(String hubs) {
            this.hubs = hubs;
        }

        public String getSnapshots() {
            return snapshots;
        }

        public void setSnapshots(String snapshots) {
            this.snapshots = snapshots;
        }
    }

    public static class Consumer {
        private String bootstrapServers;
        private String snapshotGroupId;
        private String hubGroupId;
        private String autoOffsetReset = "earliest";
        private int maxPollRecords = 10;

        public String getBootstrapServers() {
            return bootstrapServers;
        }

        public void setBootstrapServers(String bootstrapServers) {
            this.bootstrapServers = bootstrapServers;
        }

        public String getSnapshotGroupId() {
            return snapshotGroupId;
        }

        public void setSnapshotGroupId(String snapshotGroupId) {
            this.snapshotGroupId = snapshotGroupId;
        }

        public String getHubGroupId() {
            return hubGroupId;
        }

        public void setHubGroupId(String hubGroupId) {
            this.hubGroupId = hubGroupId;
        }

        public String getAutoOffsetReset() {
            return autoOffsetReset;
        }

        public void setAutoOffsetReset(String autoOffsetReset) {
            this.autoOffsetReset = autoOffsetReset;
        }

        public int getMaxPollRecords() {
            return maxPollRecords;
        }

        public void setMaxPollRecords(int maxPollRecords) {
            this.maxPollRecords = maxPollRecords;
        }
    }
}