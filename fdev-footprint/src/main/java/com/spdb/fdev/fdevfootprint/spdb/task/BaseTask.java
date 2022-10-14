package com.spdb.fdev.fdevfootprint.spdb.task;

public abstract class BaseTask implements Runnable {
	private String message;
	private String topicName;
	public BaseTask() {

	}

	public BaseTask(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	

	public String getTopicName() {
		return topicName;
	}

	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

}
