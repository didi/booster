package android.view;

public final class ViewTreeObserver {

    /**
     * Interface definition for a callback to be invoked when the view hierarchy is
     * attached to and detached from its window.
     */
    public interface OnWindowAttachListener {
        /**
         * Callback method to be invoked when the view hierarchy is attached to a window
         */
        public void onWindowAttached();

        /**
         * Callback method to be invoked when the view hierarchy is detached from a window
         */
        public void onWindowDetached();
    }

    /**
     * Interface definition for a callback to be invoked when the view hierarchy's window
     * focus state changes.
     */
    public interface OnWindowFocusChangeListener {
        /**
         * Callback method to be invoked when the window focus changes in the view tree.
         *
         * @param hasFocus
         *         Set to true if the window is gaining focus, false if it is
         *         losing focus.
         */
        public void onWindowFocusChanged(boolean hasFocus);
    }

    /**
     * Interface definition for a callback to be invoked when the focus state within
     * the view tree changes.
     */
    public interface OnGlobalFocusChangeListener {
        /**
         * Callback method to be invoked when the focus changes in the view tree. When
         * the view tree transitions from touch mode to non-touch mode, oldFocus is null.
         * When the view tree transitions from non-touch mode to touch mode, newFocus is
         * null. When focus changes in non-touch mode (without transition from or to
         * touch mode) either oldFocus or newFocus can be null.
         *
         * @param oldFocus
         *         The previously focused view, if any.
         * @param newFocus
         *         The newly focused View, if any.
         */
        public void onGlobalFocusChanged(View oldFocus, View newFocus);
    }

    /**
     * Interface definition for a callback to be invoked when the global layout state
     * or the visibility of views within the view tree changes.
     */
    public interface OnGlobalLayoutListener {
        /**
         * Callback method to be invoked when the global layout state or the visibility of views
         * within the view tree changes
         */
        public void onGlobalLayout();
    }

    /**
     * Interface definition for a callback to be invoked when the view tree is about to be drawn.
     */
    public interface OnPreDrawListener {
        /**
         * Callback method to be invoked when the view tree is about to be drawn. At this point, all
         * views in the tree have been measured and given a frame. Clients can use this to adjust
         * their scroll bounds or even to request a new layout before drawing occurs.
         *
         * @return Return true to proceed with the current drawing pass, or false to cancel.
         * @see android.view.View#onMeasure
         * @see android.view.View#onLayout
         * @see android.view.View#onDraw
         */
        public boolean onPreDraw();
    }

    /**
     * Interface definition for a callback to be invoked when the view tree is about to be drawn.
     */
    public interface OnDrawListener {
        /**
         * <p>Callback method to be invoked when the view tree is about to be drawn. At this point,
         * views cannot be modified in any way.</p>
         *
         * <p>Unlike with {@link OnPreDrawListener}, this method cannot be used to cancel the
         * current drawing pass.</p>
         *
         * <p>An {@link OnDrawListener} listener <strong>cannot be added or removed</strong>
         * from this method.</p>
         *
         * @see android.view.View#onMeasure
         * @see android.view.View#onLayout
         * @see android.view.View#onDraw
         */
        public void onDraw();
    }

    /**
     * Interface definition for a callback to be invoked when the touch mode changes.
     */
    public interface OnTouchModeChangeListener {
        /**
         * Callback method to be invoked when the touch mode changes.
         *
         * @param isInTouchMode
         *         True if the view hierarchy is now in touch mode, false  otherwise.
         */
        public void onTouchModeChanged(boolean isInTouchMode);
    }

    /**
     * Interface definition for a callback to be invoked when
     * something in the view tree has been scrolled.
     */
    public interface OnScrollChangedListener {
        /**
         * Callback method to be invoked when something in the view tree
         * has been scrolled.
         */
        public void onScrollChanged();
    }

    /**
     * Interface definition for a callback noting when a system window has been displayed.
     * This is only used for non-Activity windows. Activity windows can use
     * Activity.onEnterAnimationComplete() to get the same signal.
     *
     * @hide
     */
    public interface OnWindowShownListener {
        /**
         * Callback method to be invoked when a non-activity window is fully shown.
         */
        void onWindowShown();
    }

}
