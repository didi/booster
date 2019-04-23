package android.view;

public abstract class ViewGroup extends View implements ViewParent, ViewManager {

    /**
     * Interface definition for a callback to be invoked when the hierarchy
     * within this view changed. The hierarchy changes whenever a child is added
     * to or removed from this view.
     */
    public interface OnHierarchyChangeListener {
        /**
         * Called when a new child is added to a parent view.
         *
         * @param parent
         *         the view in which a child was added
         * @param child
         *         the new child view added in the hierarchy
         */
        void onChildViewAdded(View parent, View child);

        /**
         * Called when a child is removed from a parent view.
         *
         * @param parent
         *         the view from which the child was removed
         * @param child
         *         the child removed from the hierarchy
         */
        void onChildViewRemoved(View parent, View child);
    }

    /**
     * Register a callback to be invoked when a child is added to or removed
     * from this view.
     *
     * @param listener
     *         the callback to invoke on hierarchy change
     */
    public void setOnHierarchyChangeListener(OnHierarchyChangeListener listener) {
        throw new RuntimeException("Stub!");
    }

}
