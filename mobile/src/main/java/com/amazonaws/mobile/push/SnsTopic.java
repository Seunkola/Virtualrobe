package com.amazonaws.mobile.push;

/**
 * A simple class represents an SNS topic and its subscription status.
 */
public final class SnsTopic {
    private final String topicArn;
    private final String displayName;
    private String subscriptionArn;

    /**
     * Creates an SNS topic object. The display name is extracted from the topic ARN.
     *
     * @param topicArn topic ARN
     * @param subscriptionArn subscription ARN if known
     */
    SnsTopic(final String topicArn, final String subscriptionArn) {
        this.topicArn = topicArn;
        this.subscriptionArn = subscriptionArn;
        displayName = topicArn.substring(topicArn.lastIndexOf(':') + 1);
    }

    /**
     * Gets the topic ARN.
     *
     * @return topic ARN
     */
    public String getTopicArn() {
        return topicArn;
    }

    /**
     * Gets friendly display name.
     *
     * @return display name string
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets subscription ARN.
     *
     * @return subscription ARN
     */
    String getSubscriptionArn() {
        return subscriptionArn;
    }

    /**
     * Updates subscription ARN. Setting an empty string value to unsubscribe.
     *
     * @param subscriptionArn subscription ARN
     */
    void setSubscriptionArn(final String subscriptionArn) {
        this.subscriptionArn = subscriptionArn;
    }

    /**
     * Gets whether the topic is subscribed.
     *
     * @return true if it's subscribed, false otherwise.
     */
    public boolean isSubscribed() {
        return subscriptionArn != null && !subscriptionArn.isEmpty();
    }

    @Override
    public String toString() {
        return displayName;
    }
}
