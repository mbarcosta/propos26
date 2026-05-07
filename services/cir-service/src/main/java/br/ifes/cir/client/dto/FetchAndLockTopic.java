package br.ifes.cir.client.dto;

public class FetchAndLockTopic {

    private String topicName;
    private long lockDuration;

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public long getLockDuration() {
        return lockDuration;
    }

    public void setLockDuration(long lockDuration) {
        this.lockDuration = lockDuration;
    }
}