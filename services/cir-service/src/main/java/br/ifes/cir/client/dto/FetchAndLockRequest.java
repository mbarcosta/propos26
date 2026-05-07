package br.ifes.cir.client.dto;

import java.util.List;

public class FetchAndLockRequest {

    private String workerId;
    private int maxTasks;
    private List<FetchAndLockTopic> topics;

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public int getMaxTasks() {
        return maxTasks;
    }

    public void setMaxTasks(int maxTasks) {
        this.maxTasks = maxTasks;
    }

    public List<FetchAndLockTopic> getTopics() {
        return topics;
    }

    public void setTopics(List<FetchAndLockTopic> topics) {
        this.topics = topics;
    }
}