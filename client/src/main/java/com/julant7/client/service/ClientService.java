package com.julant7.client.service;

import com.google.protobuf.Timestamp;
import com.julant7.client.dto.InsertLogRequestDto;
import com.julant7.loglens.LogLensServiceGrpc;
import com.julant7.loglens.LogLevel;
import io.grpc.StatusRuntimeException;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ClientService {
    @GrpcClient("loglens")
    private LogLensServiceGrpc.LogLensServiceBlockingStub logLensStub;

//    public void insertLog(InsertLogRequestDto request) {
//        Instant now = Instant.now();
//
//        try {
//            var result = logLensStub.insertLog(insertRequest);
//            System.out.println("все окей");
//        } catch (StatusRuntimeException e) {
//            System.out.println("не все окей");
//            throw e;
//        }
//        System.out.println("мяу");
//    }
    private LogLevel toProtobuf(com.julant7.client.dto.LogLevel logLevel) {
        return LogLevel.valueOf(logLevel.name());
    }
}
