package org.ekkoproject.android.player.services;

import static org.ekkoproject.android.player.Constants.INVALID_COURSE;
import static org.ekkoproject.android.player.Constants.INVALID_ID;
import static org.ekkoproject.android.player.util.ThreadUtils.getLock;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class CourseManager {
    private static final AtomicLong ID = new AtomicLong(1);
    private static final Map<Long, BiMap<Long, String>> IDS = new HashMap<>();
    private static final Map<Long, Object> ID_LOCKS = new HashMap<>();

    public static String convertId(final long courseId, final long id) {
        // short-circuit for invalid id's
        if (courseId == INVALID_COURSE || id == INVALID_ID) {
            return null;
        }

        // get the BiMap
        final BiMap<Long, String> map = getMap(courseId);

        // return the BiMap value
        return map.get(id);
    }

    public static long convertId(final long courseId, final String id) {
        // short-circuit for invalid id's
        if (courseId == INVALID_COURSE || id == null) {
            return INVALID_ID;
        }

        // get the BiMap
        final BiMap<Long, String> map = getMap(courseId);

        // create new id if one doesn't exist
        synchronized (getLock(ID_LOCKS, courseId)) {
            // check to see if we already have a mapping
            Long response = map.inverse().get(id);
            if (response == null) {
                response = ID.getAndIncrement();
                map.put(response, id.intern());
            }

            // return the response
            return response;
        }
    }

    private static BiMap<Long, String> getMap(final long courseId) {
        synchronized (getLock(ID_LOCKS, courseId)) {
            // get the BiMap
            BiMap<Long, String> map = IDS.get(courseId);

            // create a BiMap if one doesn't exist
            if (map == null) {
                map = HashBiMap.create();
                IDS.put(courseId, map);
            }

            // return the BiMap
            return map;
        }
    }
}
