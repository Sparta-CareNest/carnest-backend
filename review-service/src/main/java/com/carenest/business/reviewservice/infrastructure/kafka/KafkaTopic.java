package com.carenest.business.reviewservice.infrastructure.kafka;

public enum KafkaTopic {
	REVIEW_RATING_UPDATE("review-rating-update", "리뷰 평점 업데이트 토픽");

	private final String topicName;
	private final String description;

	KafkaTopic(String topicName, String description) {
		this.topicName = topicName;
		this.description = description;
	}

	public String getTopicName() {
		return topicName;
	}

	public String getDescription() {
		return description;
	}
}