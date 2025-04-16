package com.carenest.business.reviewservice.infrastructure.kafka;

public enum KafkaTopic {
	REVIEW_RATING_UPDATE("review-rating-update");

	private final String topicName;

	KafkaTopic(String topicName) {
		this.topicName = topicName;
	}

	public String getTopicName() {
		return topicName;
	}
}