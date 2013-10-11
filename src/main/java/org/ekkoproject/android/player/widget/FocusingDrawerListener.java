package org.ekkoproject.android.player.widget;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.SimpleDrawerListener;
import android.view.View;
import android.view.ViewParent;

public class FocusingDrawerListener extends SimpleDrawerListener {
    private WeakReference<View> previous = null;

    @Override
    public void onDrawerOpened(final View drawerView) {
        super.onDrawerOpened(drawerView);

        // make sure the DrawerLayout claims focus
        final DrawerLayout layout = this.getDrawerLayout(drawerView);
        if (layout != null) {
            // track previous focus
            final Context activity = layout.getContext();
            if (activity instanceof Activity) {
                this.previous = new WeakReference<View>(((Activity) activity).getWindow().getCurrentFocus());
            }

            // now switch focus
            layout.requestFocus();
        }

    }

    @Override
    public void onDrawerClosed(final View drawerView) {
        super.onDrawerClosed(drawerView);

        // reset focus if we have a previously focused view and the drawer still
        // has focus
        if (this.previous != null) {
            final View previous = this.previous.get();
            if (previous != null) {
                final DrawerLayout layout = this.getDrawerLayout(drawerView);
                if (layout != null && layout.isFocused()) {
                    previous.requestFocus();
                }
            }
        }

        // reset previous
        this.previous = null;
    }

    private DrawerLayout getDrawerLayout(final View view) {
        ViewParent parent = view.getParent();
        while (parent != null && !(parent instanceof DrawerLayout)) {
            parent = parent.getParent();
        }
        return parent instanceof DrawerLayout ? (DrawerLayout) parent : null;
    }
}
