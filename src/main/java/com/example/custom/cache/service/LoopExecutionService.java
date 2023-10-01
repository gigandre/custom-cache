package com.example.custom.cache.service;

import java.util.Random;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

@Service
public class LoopExecutionService {
    public Long getLoopExecutionTime(int exampleParam){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Random random = new Random();
        int total = 0;
        for(int i=0; i < 100_000_000; i++){
            total = total * exampleParam * random.nextInt(100);
        }
        stopWatch.stop();
        return stopWatch.getTotalTimeMillis();

    }
}
