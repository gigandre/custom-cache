package com.example.custom.cache.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.custom.cache.annotation.MyCache;
import com.example.custom.cache.service.LoopExecutionService;

@RestController
@RequestMapping("test/cache-execution")
public class LoopExecutionController {

    @Autowired
    LoopExecutionService loopExecutionService;

    @GetMapping
    @MyCache(evictTime=1000)
    public Long getLoopExecutionTime(){
        return loopExecutionService.getLoopExecutionTime(1);
    }

    @GetMapping("/with-param")
    @MyCache(evictTime=1)
    public Long getLoopExecutionTimeWithParam(@RequestParam String testId){
        return loopExecutionService.getLoopExecutionTime(1);
    }

    
}
