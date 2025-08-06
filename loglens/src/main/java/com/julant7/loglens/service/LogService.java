package com.julant7.loglens.service;

import com.julant7.loglens.repository.LogRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class LogService {
    private LogRepository logRepository;
    
}
