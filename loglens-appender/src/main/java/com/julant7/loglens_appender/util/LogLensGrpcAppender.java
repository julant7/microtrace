package com.julant7.loglens_appender.util;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.google.protobuf.Timestamp;
import com.julant7.loglens.AppendRequest;
import com.julant7.loglens.LogLensServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;

public class LogLensGrpcAppender extends AppenderBase<ILoggingEvent> {
    @GrpcClient("loglens")
    private LogLensServiceGrpc.LogLensServiceBlockingStub logLensStub;

    @Override
    protected void append(ILoggingEvent iLoggingEvent) {
        AppendRequest request = AppendRequest.newBuilder()
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(iLoggingEvent.getTimeStamp())
                        .build())
                .setThreadName(iLoggingEvent.getThreadName())
                .setLevel(iLoggingEvent.getLevel().toString())
                .setMessage(iLoggingEvent.getMessage())
                .setLoggerName(iLoggingEvent.getLoggerName())
                .build();
    }
}
