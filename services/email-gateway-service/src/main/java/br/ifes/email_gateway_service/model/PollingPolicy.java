package br.ifes.email_gateway_service.model;

public class PollingPolicy {

    private boolean onlyUnread;
    private int maxMessagesPerPoll;
    private boolean markAsSeenWhenRead;

    public PollingPolicy() {
    }

    public boolean isOnlyUnread() {
        return onlyUnread;
    }

    public void setOnlyUnread(boolean onlyUnread) {
        this.onlyUnread = onlyUnread;
    }

    public int getMaxMessagesPerPoll() {
        return maxMessagesPerPoll;
    }

    public void setMaxMessagesPerPoll(int maxMessagesPerPoll) {
        this.maxMessagesPerPoll = maxMessagesPerPoll;
    }

    public boolean isMarkAsSeenWhenRead() {
        return markAsSeenWhenRead;
    }

    public void setMarkAsSeenWhenRead(boolean markAsSeenWhenRead) {
        this.markAsSeenWhenRead = markAsSeenWhenRead;
    }
}