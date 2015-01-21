package org.coursera.camppotlatch.service.model;

import com.google.gson.annotations.Expose;

public class OperationResult {
	public enum OperationResultState { SUCCEEDED, FAILED } 
	
	@Expose
	private OperationResultState result;
	
	public OperationResult(OperationResultState result) {
		this.result = result;
	}
	
	public OperationResultState getResult() {
		return result;
	}
	public void setResult(OperationResultState result) {
		this.result = result;
	}
}
