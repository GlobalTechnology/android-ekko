package org.ekkoproject.android.player.support.v4.adapter;

import static org.ekkoproject.android.player.Constants.INVALID_COURSE;
import static org.ekkoproject.android.player.model.Course.ENROLLMENT_TYPE_APPROVAL;
import static org.ekkoproject.android.player.model.Course.ENROLLMENT_TYPE_DISABLED;
import static org.ekkoproject.android.player.model.Course.ENROLLMENT_TYPE_OPEN;
import static org.ekkoproject.android.player.model.Course.ENROLLMENT_TYPE_UNKNOWN;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.PopupMenu;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import org.ccci.gto.android.common.api.ApiSocketException;
import org.ccci.gto.android.common.api.InvalidSessionApiException;
import org.ccci.gto.android.common.util.CursorUtils;
import org.ccci.gto.android.common.util.ViewUtils;
import org.ekkoproject.android.player.R;
import org.ekkoproject.android.player.api.EkkoHubApi;
import org.ekkoproject.android.player.db.Contract;
import org.ekkoproject.android.player.services.ProgressManager;
import org.ekkoproject.android.player.services.ResourceManager;
import org.ekkoproject.android.player.sync.EkkoSyncService;
import org.ekkoproject.android.player.tasks.LoadImageResourceAsyncTask;
import org.ekkoproject.android.player.view.ResourceImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;

public class CourseListCursorAdapter extends SimpleCursorAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(CourseListCursorAdapter.class);

    private static final String[] FROM =
            new String[] {Contract.Course.COLUMN_NAME_TITLE, Contract.Course.COLUMN_NAME_BANNER_RESOURCE,
                    Contract.Course.COLUMN_NAME_COURSE_ID};
    private static final int[] TO = new int[] {R.id.title, R.id.banner, R.id.progress};

    private final Context mContext;
    private final EkkoHubApi api;
    private final ResourceManager resourceManager;
    private final ProgressManager progressManager;

    public CourseListCursorAdapter(final Context context, final int layout) {
        super(context, layout, null, FROM, TO, 0);
        mContext = context;
        this.api = EkkoHubApi.getInstance(context);
        this.resourceManager = ResourceManager.getInstance(context);
        this.progressManager = ProgressManager.getInstance(context);
        this.setViewBinder(new CourseViewBinder());
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final View v = super.newView(context, cursor, parent);
        this.initCallbacks(new CourseViewHolder(v));
        return v;
    }

    private void initCallbacks(final CourseViewHolder holder) {
        if (holder.actionMenu != null) {
            final CoursePopupMenuClickListener listener = new CoursePopupMenuClickListener(mContext, holder);

            holder.actionMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    final PopupMenu popup = new PopupMenu(mContext, holder.actionMenu);
                    popup.setOnMenuItemClickListener(listener);
                    popup.inflate(R.menu.popup_course_card);

                    // determine menu item states
                    boolean enroll;
                    boolean unenroll;
                    boolean pending;
                    if (holder.enrolled) {
                        unenroll = true;
                        enroll = pending = false;
                    } else if (holder.pending) {
                        pending = true;
                        enroll = unenroll = false;
                    } else {
                        enroll = true;
                        pending = unenroll = false;
                    }
                    switch (holder.enrollmentType) {
                        case ENROLLMENT_TYPE_OPEN:
                            if (pending) {
                                pending = unenroll = false;
                                enroll = true;
                            }
                        case ENROLLMENT_TYPE_APPROVAL:
                            break;
                        case ENROLLMENT_TYPE_DISABLED:
                        default:
                            enroll = unenroll = pending = false;
                    }

                    // toggle menu items
                    final Menu menu = popup.getMenu();
                    if (!enroll) {
                        final MenuItem item = menu.findItem(R.id.enroll);
                        if (item != null) {
                            item.setVisible(false).setEnabled(false);
                        }
                    }
                    if (!pending) {
                        final MenuItem item = menu.findItem(R.id.pending);
                        if (item != null) {
                            item.setVisible(false).setEnabled(false);
                        }
                    }
                    if (!unenroll) {
                        final MenuItem item = menu.findItem(R.id.unenroll);
                        if (item != null) {
                            item.setVisible(false).setEnabled(false);
                        }
                    }
                    final MenuItem hide = menu.findItem(R.id.hide);
                    if (hide != null) {
                        hide.setEnabled(false);
                    }

                    // show PopupMenu
                    popup.show();
                }
            });
        }
    }

    @Override
    public void bindView(View view, Context context, Cursor c) {
        // short-circuit if we don't have a cursor or holder
        final Object holderTmp;
        if (c == null || (holderTmp = view.getTag(R.id.view_holder)) == null ||
                !(holderTmp instanceof CourseViewHolder)) {
            return;
        }
        final CourseViewHolder holder = (CourseViewHolder) holderTmp;

        // update holder values
        holder.courseId = CursorUtils.getLong(c, Contract.Course.COLUMN_NAME_COURSE_ID, INVALID_COURSE);
        holder.enrolled = CursorUtils.getBool(c, Contract.Permission.COLUMN_ENROLLED, false);
        holder.pending = CursorUtils.getBool(c, Contract.Permission.COLUMN_PENDING, false);
        holder.enrollmentType = CursorUtils.getInt(c, Contract.Course.COLUMN_ENROLLMENT_TYPE, ENROLLMENT_TYPE_UNKNOWN);

        // actually bind the view
        super.bindView(view, context, c);
    }

    private static class CourseViewHolder {
        private final View root;
        private final View actionMenu;

        private long courseId;
        private boolean enrolled = false;
        private boolean pending = false;
        private int enrollmentType = ENROLLMENT_TYPE_UNKNOWN;

        private CourseViewHolder(final View root) {
            this.root = root;
            this.actionMenu = ViewUtils.findView(root, View.class, R.id.action_menu);

            this.attach();
        }

        private void attach() {
            // attach the holder to the various views
            for (final View view : new View[] {this.root, this.actionMenu}) {
                if (view == null) {
                    continue;
                }

                view.setTag(R.id.view_holder, this);
            }
        }
    }

    private static class CoursePopupMenuClickListener implements PopupMenu.OnMenuItemClickListener {
        private final Context mContext;
        private final EkkoHubApi api;
        private final CourseViewHolder holder;

        private CoursePopupMenuClickListener(final Context context, final CourseViewHolder holder) {
            mContext = context;
            this.api = EkkoHubApi.getInstance(context);
            this.holder = holder;
        }

        @Override
        public boolean onMenuItemClick(final MenuItem item) {
            final int id = item.getItemId();
            switch(id) {
                case R.id.enroll:
                case R.id.unenroll:
                    this.api.async(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                switch(id) {
                                    case R.id.enroll:
                                        api.enroll(holder.courseId);
                                        break;
                                    case R.id.unenroll:
                                        api.unenroll(holder.courseId);
                                        break;
                                }

                                // sync the course now that we enrolled/unenrolled
                                EkkoSyncService.syncCourse(mContext, holder.courseId);
                            } catch (final ApiSocketException e) {
                            } catch (final InvalidSessionApiException e) {
                            }
                        }
                    });

                    return true;
            }

            return false;
        }
    }

    private class CourseViewBinder implements SimpleCursorAdapter.ViewBinder {
        @Override
        public boolean setViewValue(final View view, final Cursor c, final int columnIndex) {
            switch (view.getId()) {
                case R.id.banner:
                    if (view instanceof ResourceImageView) {
                        ((ResourceImageView) view).setResource(
                                c.getLong(c.getColumnIndex(Contract.Course.COLUMN_NAME_COURSE_ID)),
                                c.getString(columnIndex));
                    } else if (view instanceof ImageView) {
                        ((ImageView) view).setImageDrawable(null);
                        new LoadImageResourceAsyncTask(resourceManager, (ImageView) view, c.getLong(
                                c.getColumnIndex(Contract.Course.COLUMN_NAME_COURSE_ID)), c.getString(columnIndex))
                                .execute();
                    }
                    return true;
                case R.id.progress:
                    if (view instanceof ProgressBar) {
                        ((ProgressBar) view).setProgress(0);
                        new UpdateProgressBarAsyncTask((ProgressBar) view).execute(c.getLong(c.getColumnIndex(
                                Contract.Course.COLUMN_NAME_COURSE_ID)));
                    }
                    return true;
            }

            return false;
        }
    }

    private class UpdateProgressBarAsyncTask extends AsyncTask<Long, Void, Pair<Integer, Integer>> {
        private final WeakReference<ProgressBar> progressBar;

        protected UpdateProgressBarAsyncTask(final ProgressBar progressBar) {
            progressBar.setTag(R.id.progress_bar_update_task, new WeakReference<AsyncTask<?, ?, ?>>(this));
            this.progressBar = new WeakReference<ProgressBar>(progressBar);
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public AsyncTask<Long, Void, Pair<Integer, Integer>> execute(final long courseId) {
            final Long[] params = new Long[] {courseId};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                return this.executeOnExecutor(THREAD_POOL_EXECUTOR, params);
            } else {
                return this.execute(params);
            }
        }

        @Override
        protected Pair<Integer, Integer> doInBackground(final Long... params) {
            if (params.length > 0 && this.checkProgressBar()) {
                return progressManager.getCourseProgress(params[0]);
            }

            // default to no progress
            return Pair.create(0, 1);
        }

        @Override
        protected void onPostExecute(final Pair<Integer, Integer> progress) {
            super.onPostExecute(progress);
            if (this.checkProgressBar()) {
                final ProgressBar progressBar = this.progressBar.get();
                if (progressBar != null) {
                    progressBar.setMax(progress.second);
                    progressBar.setProgress(progress.first);
                }
            }
        }

        private boolean checkProgressBar() {
            final ProgressBar progressBar = this.progressBar.get();
            if (progressBar != null) {
                final Object ref = progressBar.getTag(R.id.progress_bar_update_task);
                if (ref instanceof WeakReference) {
                    final Object task = ((WeakReference<?>) ref).get();
                    return this == task;
                }
            }
            return false;
        }
    }
}
