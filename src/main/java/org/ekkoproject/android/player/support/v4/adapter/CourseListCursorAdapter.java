package org.ekkoproject.android.player.support.v4.adapter;

import static org.ekkoproject.android.player.Constants.INVALID_COURSE;
import static org.ekkoproject.android.player.model.Course.ENROLLMENT_TYPE_APPROVAL;
import static org.ekkoproject.android.player.model.Course.ENROLLMENT_TYPE_DISABLED;
import static org.ekkoproject.android.player.model.Course.ENROLLMENT_TYPE_OPEN;
import static org.ekkoproject.android.player.model.Course.ENROLLMENT_TYPE_UNKNOWN;
import static org.ekkoproject.android.player.tasks.EnrollmentRunnable.ENROLL;
import static org.ekkoproject.android.player.tasks.EnrollmentRunnable.UNENROLL;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.PopupMenu;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import org.ccci.gto.android.common.util.CursorUtils;
import org.ccci.gto.android.common.util.ViewUtils;
import org.ekkoproject.android.player.OnNavigationListener;
import org.ekkoproject.android.player.R;
import org.ekkoproject.android.player.db.Contract;
import org.ekkoproject.android.player.db.EkkoDao;
import org.ekkoproject.android.player.model.EnrollmentState;
import org.ekkoproject.android.player.model.Permission;
import org.ekkoproject.android.player.services.ProgressManager;
import org.ekkoproject.android.player.services.ResourceManager;
import org.ekkoproject.android.player.support.v4.fragment.NotEnrolledDialogFragment;
import org.ekkoproject.android.player.sync.EkkoSyncService;
import org.ekkoproject.android.player.tasks.EnrollmentRunnable;
import org.ekkoproject.android.player.tasks.LoadImageResourceAsyncTask;
import org.ekkoproject.android.player.view.ResourceImageView;

import java.lang.ref.WeakReference;

public class CourseListCursorAdapter extends SimpleCursorAdapter {
    private static final String[] FROM =
            new String[] {Contract.Course.COLUMN_NAME_TITLE, Contract.Course.COLUMN_NAME_BANNER_RESOURCE,
                    Contract.Course.COLUMN_NAME_COURSE_ID};
    private static final int[] TO = new int[] {R.id.title, R.id.banner, R.id.progress};

    private final FragmentActivity mActivity;
    private final String mGuid;
    private final ResourceManager resourceManager;
    private final ProgressManager progressManager;

    private OnNavigationListener mOnNavigationListener = null;

    public CourseListCursorAdapter(final FragmentActivity activity, final String guid, final int layout) {
        super(activity, layout, null, FROM, TO, 0);
        mActivity = activity;
        mGuid = guid;
        this.resourceManager = ResourceManager.getInstance(activity);
        this.progressManager = ProgressManager.getInstance(activity, mGuid);
        this.setViewBinder(new CourseViewBinder());
    }

    public void setOnNavigationListener(final OnNavigationListener listener) {
        this.mOnNavigationListener = listener;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final View v = super.newView(context, cursor, parent);
        this.initCallbacks(new CourseViewHolder(v));
        return v;
    }

    private void initCallbacks(final CourseViewHolder holder) {
        final View actionMenu = holder.actionMenu();
        if (actionMenu != null) {
            actionMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    final PopupMenu popup = new PopupMenu(mActivity, actionMenu);
                    final CoursePopupMenuClickListener listener =
                            new CoursePopupMenuClickListener(mActivity, mGuid, holder);
                    listener.setOnNavigationListener(mOnNavigationListener);
                    popup.setOnMenuItemClickListener(listener);
                    popup.inflate(R.menu.popup_course_card);
                    final Menu menu = popup.getMenu();

                    // toggle enrollment MenuItem visibility
                    {
                        // calculate enrollment state
                        EnrollmentState state = EnrollmentState.DISABLED;
                        if (holder.enrolled) {
                            state = EnrollmentState.ENROLLED;
                        } else if (holder.pending) {
                            state = EnrollmentState.PENDING;
                        } else {
                            state = EnrollmentState.UNENROLLED;
                        }
                        switch (holder.enrollmentType) {
                            case ENROLLMENT_TYPE_OPEN:
                                if (state == EnrollmentState.PENDING) {
                                    state = EnrollmentState.UNENROLLED;
                                }
                            case ENROLLMENT_TYPE_APPROVAL:
                                break;
                            case ENROLLMENT_TYPE_DISABLED:
                            default:
                                state = EnrollmentState.DISABLED;
                        }

                        // toggle MenuItem visibility
                        menu.setGroupVisible(R.id.enrollment, false);
                        final MenuItem item;
                        switch (state) {
                            case UNENROLLED:
                                item = menu.findItem(R.id.enroll);
                                break;
                            case PENDING:
                                item = menu.findItem(R.id.pending);
                                break;
                            case ENROLLED:
                                item = menu.findItem(R.id.unenroll);
                                break;
                            default:
                                item = null;
                        }
                        if (item != null) {
                            item.setVisible(true);
                        }
                    }

                    // toggle My Courses show/hide MenuItem visibility
                    menu.setGroupVisible(R.id.visibility, false);
                    if (holder.contentVisible) {
                        final MenuItem item = menu.findItem(holder.hidden ? R.id.show : R.id.hide);
                        if (item != null) {
                            item.setVisible(true);
                        }
                    }

                    // show PopupMenu
                    popup.show();
                }
            });
        }

        final View root = holder.root();
        if (root != null) {
            root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    // Create and show the login dialog only if it is not currently displayed
                    final FragmentManager fm = mActivity.getSupportFragmentManager();
                    fm.popBackStack("enrollDialog", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    NotEnrolledDialogFragment.newInstance(holder.courseId, mGuid).show(
                            fm.beginTransaction().addToBackStack("enrollDialog"), "enrollDialog");
                }
            });

            // disable the click listener for now
            root.setClickable(false);
        }
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor c) {
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
        holder.contentVisible = CursorUtils.getBool(c, Contract.Permission.COLUMN_CONTENT_VISIBLE, false);
        holder.pending = CursorUtils.getBool(c, Contract.Permission.COLUMN_PENDING, false);
        holder.hidden = CursorUtils.getBool(c, Contract.Permission.COLUMN_HIDDEN, false);
        holder.enrollmentType = CursorUtils.getInt(c, Contract.Course.COLUMN_ENROLLMENT_TYPE, ENROLLMENT_TYPE_UNKNOWN);

        // enable/disable not enrolled popup based on whether the content is visible
        final View root = holder.root();
        if (root != null) {
            root.setClickable(!holder.contentVisible);
        }

        // actually bind the view
        super.bindView(view, context, c);
    }

    @SuppressLint("ViewTag")
    private static class CourseViewHolder {
        // we use WeakReferences for views in the ViewHolder because of potential memory leaks with setTag in Android < 4.0
        private final WeakReference<View> root;
        private final WeakReference<View> actionMenu;

        private long courseId;
        private boolean enrolled = false;
        private boolean pending = false;
        private boolean contentVisible = false;
        private boolean hidden = false;
        private int enrollmentType = ENROLLMENT_TYPE_UNKNOWN;

        private CourseViewHolder(final View root) {
            this.root = new WeakReference<>(root);
            this.actionMenu = new WeakReference<>(ViewUtils.findView(root, View.class, R.id.action_menu));

            this.attach();
        }

        private void attach() {
            // attach the holder to the various views
            for (final View view : new View[] {this.root(), this.actionMenu()}) {
                if (view == null) {
                    continue;
                }

                view.setTag(R.id.view_holder, this);
            }
        }

        private View root() {
            return this.root.get();
        }

        private View actionMenu() {
            return this.actionMenu.get();
        }
    }

    private static class CoursePopupMenuClickListener implements PopupMenu.OnMenuItemClickListener {
        private final Context mContext;
        private final String mGuid;
        private final EkkoDao dao;
        private final CourseViewHolder holder;

        private OnNavigationListener mOnNavigationListener = null;

        private CoursePopupMenuClickListener(final Context context, final String guid, final CourseViewHolder holder) {
            mContext = context;
            mGuid = guid;
            this.dao = EkkoDao.getInstance(context);
            this.holder = holder;
        }

        public void setOnNavigationListener(final OnNavigationListener listener) {
            this.mOnNavigationListener = listener;
        }

        @Override
        public boolean onMenuItemClick(final MenuItem item) {
            final int id = item.getItemId();
            switch(id) {
                case R.id.enroll:
                    final EnrollmentRunnable task = new EnrollmentRunnable(mContext, mGuid, ENROLL, holder.courseId);
                    task.setOnNavigationListener(mOnNavigationListener);
                    task.schedule();
                    return true;
                case R.id.unenroll:
                    new EnrollmentRunnable(mContext, mGuid, UNENROLL, holder.courseId).schedule();
                    return true;
                case R.id.show:
                case R.id.hide:
                    final Permission permission = new Permission(holder.courseId, mGuid);
                    permission.setHidden(id == R.id.hide);
                    this.dao.async(new Runnable() {
                        @Override
                        public void run() {
                            dao.update(permission, new String[] {Contract.Permission.COLUMN_HIDDEN});

                            //XXX: this is a quick hack, we should move the course/manifest update broadcasts to a common service
                            EkkoSyncService.broadcastCoursesUpdate(mContext);
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
            this.progressBar = new WeakReference<>(progressBar);
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
