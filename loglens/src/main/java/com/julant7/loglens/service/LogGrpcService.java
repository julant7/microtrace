package com.julant7.loglens.service;

import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import com.julant7.loglens.InsertRequest;
import com.julant7.loglens.LogLensServiceGrpc;
import com.julant7.loglens.entity.Span;
import com.julant7.loglens.entity.LogLevel;
import com.julant7.loglens.repository.LogRepository;
import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.Instant;

@Slf4j
@AllArgsConstructor
@GrpcService
public class LogGrpcService extends LogLensServiceGrpc.LogLensServiceImplBase {
    private LogRepository logRepository;
    @Override
    public void insertLog(InsertRequest insertRequest, StreamObserver<Empty> responseObserver) {
        try {
            Timestamp timestamp = insertRequest.getTimestamp();
//            LogEntity logEntity = new LogEntity(insertRequest.getServiceName(),
//                    insertRequest.getMessage(),
//                    Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos()),
//                    toEnum(insertRequest.getLogLevel())
//                    );
            Span span = new Span();
            span.setMessage(insertRequest.getMessage());
            span.setService(insertRequest.getServiceName());
            span.setTimestamp(Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos()));
            span.setLogLevel(toEnum(insertRequest.getLogLevel()));
//            LogEntity logEntity = LogEntity.builder()
//                    .service(insertRequest.getServiceName())
//                    .timestamp(Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos()))
//                    .message(insertRequest.getMessage())
//                    .logLevel(toEnum(insertRequest.getLogLevel()))
//                    .build();
            logRepository.save(span);
            log.error("yes");
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
    private LogLevel toEnum(com.julant7.loglens.LogLevel logLevel) {
        return LogLevel.valueOf(logLevel.name());
    }
}
