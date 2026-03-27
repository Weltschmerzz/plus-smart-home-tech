package ru.yandex.practicum.collector.mapper.proto;

import com.google.protobuf.Timestamp;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ProtoTimestampMapper {

    public Instant toInstant(Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }
}