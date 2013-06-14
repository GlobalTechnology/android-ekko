package org.ekkoproject.android.player.adapter;

import static org.ekkoproject.android.player.Constants.DEFAULT_LAYOUT;
import static org.ekkoproject.android.player.util.ViewUtils.assertValidLayout;

import org.ekkoproject.android.player.model.Manifest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class ManifestAdapter<T> extends BaseAdapter {
    private Manifest manifest = null;

    private LayoutInflater mInflater;
    private int layout = DEFAULT_LAYOUT;

    public ManifestAdapter(final Context context) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public final synchronized Manifest swapManifest(final Manifest manifest) {
        final Manifest old = this.manifest;
        this.onNewManifest(manifest);
        if (this.manifest != null) {
            notifyDataSetChanged();
        } else {
            notifyDataSetInvalidated();
        }

        return old;
    }

    protected void onNewManifest(final Manifest manifest) {
        this.manifest = manifest;
    }

    protected Manifest getManifest() {
        return this.manifest;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        final int layout = getLayout(getItemViewType(position));
        assertValidLayout(layout);
        final View v = mInflater.inflate(layout, parent, false);
        bindView(v, getItem(position));
        return v;
    }

    protected int getLayout(final int viewType) {
        return getLayout();
    }

    public int getLayout() {
        return this.layout;
    }

    public void setLayout(final int layout) {
        this.layout = layout;
    }

    protected abstract void bindView(View v, T object);

    @Override
    public abstract T getItem(int position);
}
