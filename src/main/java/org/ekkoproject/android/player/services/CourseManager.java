package org.ekkoproject.android.player.services;

import static org.ekkoproject.android.player.Constants.INVALID_COURSE;
import static org.ekkoproject.android.player.Constants.INVALID_ID;
import static org.ekkoproject.android.player.util.ThreadUtils.getLock;

import org.ekkoproject.android.player.util.BidiMap;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class CourseManager {
    private static final AtomicLong nextId = new AtomicLong(1);
    private static final Map<Long, BidiMap<Long, String>> idMap = new HashMap<Long, BidiMap<Long, String>>();
    private static final Map<Long, Object> idMapLocks = new HashMap<Long, Object>();

    public static String convertId(final long courseId, final long id) {
        // short-circuit for invalid id's
        if (courseId == INVALID_COURSE || id == INVALID_ID) {
            return null;
        }

        // get the BidiMap
        final BidiMap<Long, String> map = getMap(courseId);

        // return the BidiMap value
        return map.get(id);
    }

    public static long convertId(final long courseId, final String id) {
        // short-circuit for invalid id's
        if (courseId == INVALID_COURSE || id == null) {
            return INVALID_ID;
        }

        // get the BidiMap
        final BidiMap<Long, String> map = getMap(courseId);

        // create new id if one doesn't exist
        synchronized (getLock(idMapLocks, courseId)) {
            // check to see if we already have a mapping
            Long response = map.getKey(id);
            if (response == null) {
                response = nextId.getAndIncrement();
                map.put(response, id.intern());
            }

            // return the response
            return response;
        }
    }

    private static BidiMap<Long, String> getMap(final long courseId) {
        synchronized (getLock(idMapLocks, courseId)) {
            // get the BidiMap
            BidiMap<Long, String> map = idMap.get(courseId);

            // create a BidiMap if one doesn't exist
            if (map == null) {
                map = new BidiMap<Long, String>();
                idMap.put(courseId, map);
            }

            // return the BidiMap
            return map;
        }
    }
}
