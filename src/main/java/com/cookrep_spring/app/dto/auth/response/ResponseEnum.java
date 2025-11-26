package com.cookrep_spring.app.dto.auth.response;

public enum ResponseEnum {

	SUCCESS(0, "success"), FAIL(-1, "fail");

	int code;
	String msg;

	private ResponseEnum(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	public int getCode() {
		return code;
	}

	public String getMsg() {
		return msg;
	}

}
