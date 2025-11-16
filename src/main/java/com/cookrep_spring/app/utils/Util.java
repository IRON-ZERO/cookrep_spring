package com.cookrep_spring.app.utils;

import java.util.UUID;

import jakarta.servlet.http.Cookie;

public class Util {

	public final static String ACCESS_TOKEN = "access_token";
	public final static String REFRESH_TOKEN = "refresh_token";

	public static String UUIDGenerator() {
		return UUID.randomUUID().toString();
	}

	public static Cookie buildCookie(String cookieName, String value, int maxAge) {
		Cookie cookie = new Cookie(cookieName, value);
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setPath("/");
		cookie.setMaxAge(maxAge);
		return cookie;
	}
}
