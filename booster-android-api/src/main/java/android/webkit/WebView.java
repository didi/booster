package android.webkit;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsoluteLayout;

public class WebView extends AbsoluteLayout implements ViewTreeObserver.OnGlobalFocusChangeListener, ViewGroup.OnHierarchyChangeListener, ViewDebug.HierarchyHandler {

    /**
     * Constructs a new WebView with a Context object.
     *
     * @param context a Context object used to access application assets
     */
    public WebView(Context context) {
        this(context, null);
    }

    /**
     * Constructs a new WebView with layout parameters.
     *
     * @param context a Context object used to access application assets
     * @param attrs an AttributeSet passed to our parent
     */
    public WebView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Constructs a new WebView with layout parameters and a default style.
     *
     * @param context a Context object used to access application assets
     * @param attrs an AttributeSet passed to our parent
     * @param defStyleAttr an attribute in the current theme that contains a
     *        reference to a style resource that supplies default values for
     *        the view. Can be 0 to not look for defaults.
     */
    public WebView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    /**
     * Constructs a new WebView with layout parameters and a default style.
     *
     * @param context a Context object used to access application assets
     * @param attrs an AttributeSet passed to our parent
     * @param defStyleAttr an attribute in the current theme that contains a
     *        reference to a style resource that supplies default values for
     *        the view. Can be 0 to not look for defaults.
     * @param defStyleRes a resource identifier of a style resource that
     *        supplies default values for the view, used only if
     *        defStyleAttr is 0 or can not be found in the theme. Can be 0
     *        to not look for defaults.
     */
    public WebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Constructs a new WebView with layout parameters and a default style.
     *
     * @param context a Context object used to access application assets
     * @param attrs an AttributeSet passed to our parent
     * @param defStyleAttr an attribute in the current theme that contains a
     *        reference to a style resource that supplies default values for
     *        the view. Can be 0 to not look for defaults.
     * @param privateBrowsing whether this WebView will be initialized in
     *                        private mode
     *
     * @deprecated Private browsing is no longer supported directly via
     * WebView and will be removed in a future release. Prefer using
     * {@link WebSettings}, {@link WebViewDatabase}, {@link CookieManager}
     * and {@link WebStorage} for fine-grained control of privacy data.
     */
    @Deprecated
    public WebView(Context context, AttributeSet attrs, int defStyleAttr, boolean privateBrowsing) {
        throw new RuntimeException("Stub!");
    }


    @Override
    public void onChildViewAdded(View parent, View child) {
    }

    @Override
    public void onChildViewRemoved(View parent, View child) {
    }

    @Override
    public void onGlobalFocusChanged(View oldFocus, View newFocus) {
    }

    public static void setDataDirectorySuffix(String suffix) {
        throw new RuntimeException("Stub!");
    }

}
