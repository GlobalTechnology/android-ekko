package org.ekkoproject.android.player.support.v4.fragment;

import java.util.Collections;
import java.util.Set;

import org.ekkoproject.android.player.services.EkkoBroadcastReceiver;
import org.ekkoproject.android.player.tasks.UpdateProgressAsyncTask;

public abstract class AbstractManifestAndProgressAwareFragment extends AbstractManifestAwareFragment implements
        EkkoBroadcastReceiver.ProgressUpdateListener {
    private static final Set<String> NO_PROGRESS = Collections.emptySet();

    private Set<String> progress = NO_PROGRESS;

    /** BEGIN lifecycle */

    @Override
    public void onStart() {
        super.onStart();
        this.updateProgress();
    }

    @Override
    public final void onProgressUpdate(final long courseId) {
        this.updateProgress();
    }

    protected void onProgressUpdate(final Set<String> progress) {
        this.progress = progress;
    }

    /** END lifecycle */

    protected Set<String> getProgress() {
        return this.progress;
    }

    private void updateProgress() {
        new UpdateProgressAsyncTask(this.getProgressManager()) {
            @Override
            protected void onPostExecute(final Set<String> result) {
                super.onPostExecute(result);
                AbstractManifestAndProgressAwareFragment.this.onProgressUpdate(result);
            }
        }.execute(this.getCourseId());
    }
}
