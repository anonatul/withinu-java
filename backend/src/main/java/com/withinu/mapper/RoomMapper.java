package com.withinu.mapper;

import com.withinu.dto.RoomResponse;
import com.withinu.entity.Room;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

public final class RoomMapper {

    private RoomMapper() {
    }

    public static RoomResponse toResponse(Room room) {
        return new RoomResponse(room.getId(), room.getName(), room.getSlug(),
            room.getDescription(), 0, null);
    }

    public static RoomResponse fromStatsRow(Object[] row) {
        return new RoomResponse(
            asUuid(row[0]),
            (String) row[1],
            (String) row[2],
            (String) row[3],
            asLong(row[4]),
            asInstant(row[5])
        );
    }

    private static UUID asUuid(Object o) {
        return o instanceof UUID u ? u : UUID.fromString(o.toString());
    }

    private static long asLong(Object o) {
        return o instanceof Number n ? n.longValue() : 0L;
    }

    private static Instant asInstant(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Instant i) {
            return i;
        }
        if (o instanceof java.sql.Timestamp t) {
            return t.toInstant();
        }
        if (o instanceof LocalDateTime ldt) {
            return ldt.atZone(ZoneId.systemDefault()).toInstant();
        }
        return null;
    }
}