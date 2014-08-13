package org.ekkoproject.android.player.model;

import static org.ekkoproject.android.player.model.Course.ENROLLMENT_TYPE_APPROVAL;
import static org.ekkoproject.android.player.model.Course.ENROLLMENT_TYPE_DISABLED;
import static org.ekkoproject.android.player.model.Course.ENROLLMENT_TYPE_OPEN;

public enum EnrollmentState {
    DISABLED, UNENROLLED, PENDING, ENROLLED, UNKNOWN;

    public static EnrollmentState determineState(final int enrollmentType, final boolean enrolled,
                                                 final boolean pending) {
        // calculate enrollment state
        EnrollmentState state;
        if (enrolled) {
            state = ENROLLED;
        } else if (pending) {
            state = PENDING;
        } else {
            state = UNENROLLED;
        }
        switch (enrollmentType) {
            case ENROLLMENT_TYPE_OPEN:
                if (state == PENDING) {
                    state = UNENROLLED;
                }
            case ENROLLMENT_TYPE_APPROVAL:
                break;
            case ENROLLMENT_TYPE_DISABLED:
                state = DISABLED;
                break;
            default:
                state = UNKNOWN;
        }
        return state;
    }
}
