package nz.ac.canterbury.seng302.identityprovider.mapping;

import org.springframework.stereotype.Component;

/**
 * Provides a utility method for mapping between different representations of Timestamps.
 */
@Component
public class TimestampMapper {

    /**
     * Maps a java.sql.Timestamp to a com.google.protobuf.Timestamp
     * @param timestamp A java.sql.Timestamp
     * @return An equivalent com.google.protobuf.Timestamp
     */
    public com.google.protobuf.Timestamp toProtobufTimestamp(java.sql.Timestamp timestamp) {
        return com.google.protobuf.Timestamp
                .newBuilder()
                .setSeconds(timestamp.getTime() / 1000)
                .setNanos(timestamp.getNanos())
                .build();
    }
}
