package org.ekkoproject.android.player;

import android.content.Intent;
import android.content.IntentFilter;

public final class BroadcastUtils {
    // actions this can be  broadcast
    private static final String ACTION_UPDATE_COURSES =
            BroadcastUtils.class.getPackage().getName() + ".ACTION_UPDATE_COURSES";

    // extra data for broadcast intents
    public static final String EXTRA_COURSES = BroadcastUtils.class.getPackage().getName() + ".EXTRA_COURSES";

    /* Broadcast Intent generation methods */

    public static Intent updateCoursesBroadcast(final long... courses) {
        final Intent intent = new Intent(ACTION_UPDATE_COURSES);
        intent.putExtra(EXTRA_COURSES, courses);
        return intent;
    }

    /* Intent Filter generation methods */

    public static IntentFilter updateCoursesFilter() {
        return new IntentFilter(ACTION_UPDATE_COURSES);
    }
}
