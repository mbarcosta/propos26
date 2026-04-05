package br.ifes.email_gateway_service.model;

public class IngestionPolicy {

    private boolean includeBody;
    private boolean includeAttachmentMetadata;
    private boolean includeAttachmentContent;
    private boolean ignoreAutoReply;

    public IngestionPolicy() {
    }

    public boolean isIncludeBody() {
        return includeBody;
    }

    public void setIncludeBody(boolean includeBody) {
        this.includeBody = includeBody;
    }

    public boolean isIncludeAttachmentMetadata() {
        return includeAttachmentMetadata;
    }

    public void setIncludeAttachmentMetadata(boolean includeAttachmentMetadata) {
        this.includeAttachmentMetadata = includeAttachmentMetadata;
    }

    public boolean isIncludeAttachmentContent() {
        return includeAttachmentContent;
    }

    public void setIncludeAttachmentContent(boolean includeAttachmentContent) {
        this.includeAttachmentContent = includeAttachmentContent;
    }

    public boolean isIgnoreAutoReply() {
        return ignoreAutoReply;
    }

    public void setIgnoreAutoReply(boolean ignoreAutoReply) {
        this.ignoreAutoReply = ignoreAutoReply;
    }
}