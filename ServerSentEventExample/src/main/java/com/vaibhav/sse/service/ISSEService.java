package com.vaibhav.sse.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface ISSEService {

	public SseEmitter startSSEEmitter();
}
