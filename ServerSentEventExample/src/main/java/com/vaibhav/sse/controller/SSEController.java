package com.vaibhav.sse.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.vaibhav.sse.service.ISSEService;

@RestController
@RequestMapping("/api/sse/")
public class SSEController {

	@Autowired
	ISSEService sseService;
	
	@GetMapping("create")
    public SseEmitter ping() {
        return sseService.startSSEEmitter();
    }
}
