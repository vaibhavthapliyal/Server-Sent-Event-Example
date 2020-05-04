package com.vaibhav.sse.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.vaibhav.sse.service.ISSEService;

@Service
public class SSEServiceImpl implements ISSEService {

	private Logger log = LogManager.getLogger(SSEServiceImpl.class);
	
	@Value("${events.number}")
	private int numberOfEvents;
	
	@Value("${threads.number}")
	private int numberOfThreads;
	
	private AtomicInteger eventsProcessed;
	

	private CountDownLatch latch; 
	
	private ExecutorService service;
	
	@PostConstruct
	private void init() {
		service = Executors.newFixedThreadPool(numberOfThreads);
	}
	
	
	@Override
	public SseEmitter startSSEEmitter() {
		
		eventsProcessed = new AtomicInteger(0);
		
		latch = new CountDownLatch(numberOfThreads);
		
		SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
		
		
		List<Integer> sourceArray = new ArrayList<>();
		
		for(int i = 1; i<=numberOfEvents; i++) {
			sourceArray.add(i);
		}
		
		if(!sourceArray.isEmpty()) {
			int numOfRecords = sourceArray.size();
			log.info("Number of Records: " + numOfRecords);
			int start = 0;
			int range = (numOfRecords / numberOfThreads);
			int end = range;
			
			int threadIndex = 0;
			
			if (range != 0) {
				while (end <= numOfRecords) {					
					service.execute(
							new EmitterThread(sourceArray.subList(start, end), threadIndex, emitter));
					start = end;
					end = end + range;
					threadIndex++;
				}
			}
		}
		
		return emitter;
	}
	
	public class EmitterThread implements Runnable{

		private  List<Integer> sourceArray;

		private int threadIndex = 1;
		
		private SseEmitter emitter;
		
		public EmitterThread(List<Integer> sourceArray, int threadIndex, SseEmitter emitter) {
			this.sourceArray = sourceArray;
			this.threadIndex = threadIndex;
			this.emitter = emitter;
		}
		
		@Override
		public void run() {
			log.info("Processing batch from Index: " + threadIndex);

			try {
				log.info("Batch Number: " + threadIndex + " Size: " +sourceArray.size());
				
				for (int i = 0; i<sourceArray.size(); i++) {
					log.info("Sleeping for 100 ms");
					Thread.sleep(100);
					//log.info("Processed " + eventsProcessed.incrementAndGet() + " events out of " + numberOfEvents + " events");
					emitter.send("Processed " + eventsProcessed.incrementAndGet() + " events out of " + numberOfEvents + " events", MediaType.TEXT_PLAIN);
				}
				
				latch.countDown();
				latch.await();
				emitter.complete();
			}catch(Exception e) {
				log.error("Some Error Ocurred", e);
			}
			
		}
		
	}

}
