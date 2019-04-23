package android.os;

public class Build {

    public static final String UNKNOWN = "unknown";

    /**
     * Either a changelist number, or a label like "M4-rc20".
     */
    public static final String ID = getString("ro.build.id");

    /**
     * A build ID string meant for displaying to the user
     */
    public static final String DISPLAY = getString("ro.build.display.id");

    /**
     * The name of the overall product.
     */
    public static final String PRODUCT = getString("ro.product.name");

    /**
     * The name of the industrial design.
     */
    public static final String DEVICE = getString("ro.product.device");

    /**
     * The name of the underlying board, like "goldfish".
     */
    public static final String BOARD = getString("ro.product.board");

    /**
     * The manufacturer of the product/hardware.
     */
    public static final String MANUFACTURER = getString("ro.product.manufacturer");

    /**
     * The consumer-visible brand with which the product/hardware will be associated, if any.
     */
    public static final String BRAND = getString("ro.product.brand");

    /**
     * The end-user-visible name for the end product.
     */
    public static final String MODEL = getString("ro.product.model");

    /**
     * The system bootloader version number.
     */
    public static final String BOOTLOADER = getString("ro.bootloader");

    /**
     * The name of the hardware (from the kernel command line or /proc).
     */
    public static final String HARDWARE = getString("ro.hardware");

    /**
     * Whether this build was for an emulator device.
     */
    public static final boolean IS_EMULATOR = getString("ro.kernel.qemu").equals("1");

    /**
     * An ordered list of ABIs supported by this device. The most preferred ABI is the first
     * element in the list.
     * <p>
     * See {@link #SUPPORTED_32_BIT_ABIS} and {@link #SUPPORTED_64_BIT_ABIS}.
     */
    public static final String[] SUPPORTED_ABIS = getStringList("ro.product.cpu.abilist", ",");

    /**
     * An ordered list of <b>32 bit</b> ABIs supported by this device. The most preferred ABI
     * is the first element in the list.
     * <p>
     * See {@link #SUPPORTED_ABIS} and {@link #SUPPORTED_64_BIT_ABIS}.
     */
    public static final String[] SUPPORTED_32_BIT_ABIS = getStringList("ro.product.cpu.abilist32", ",");

    /**
     * An ordered list of <b>64 bit</b> ABIs supported by this device. The most preferred ABI
     * is the first element in the list.
     * <p>
     * See {@link #SUPPORTED_ABIS} and {@link #SUPPORTED_32_BIT_ABIS}.
     */
    public static final String[] SUPPORTED_64_BIT_ABIS = getStringList("ro.product.cpu.abilist64", ",");

    public static class VERSION {

        /**
         * The internal value used by the underlying source control to
         * represent this build.  E.g., a perforce changelist number
         * or a git hash.
         */
        public static final String INCREMENTAL = getString("ro.build.version.incremental");

        /**
         * The user-visible version string.  E.g., "1.0" or "3.4b5".
         */
        public static final String RELEASE = getString("ro.build.version.release");

        /**
         * The base OS build the product is based on.
         */
        public static final String BASE_OS = SystemProperties.get("ro.build.version.base_os", "");

        /**
         * The user-visible security patch level.
         */
        public static final String SECURITY_PATCH = SystemProperties.get("ro.build.version.security_patch", "");

        /**
         * The user-visible SDK version of the framework in its raw String
         * representation; use {@link #SDK_INT} instead.
         *
         * @deprecated Use {@link #SDK_INT} to easily get this as an integer.
         */
        @Deprecated
        public static final String SDK = getString("ro.build.version.sdk");

        /**
         * The user-visible SDK version of the framework; its possible
         * values are defined in {@link Build.VERSION_CODES}.
         */
        public static final int SDK_INT = SystemProperties.getInt("ro.build.version.sdk", 0);

        /**
         * The developer preview revision of a prerelease SDK. This value will always
         * be <code>0</code> on production platform builds/devices.
         *
         * <p>When this value is nonzero, any new API added since the last
         * officially published {@link #SDK_INT API level} is only guaranteed to be present
         * on that specific preview revision. For example, an API <code>Activity.fooBar()</code>
         * might be present in preview revision 1 but renamed or removed entirely in
         * preview revision 2, which may cause an app attempting to call it to crash
         * at runtime.</p>
         *
         * <p>Experimental apps targeting preview APIs should check this value for
         * equality (<code>==</code>) with the preview SDK revision they were built for
         * before using any prerelease platform APIs. Apps that detect a preview SDK revision
         * other than the specific one they expect should fall back to using APIs from
         * the previously published API level only to avoid unwanted runtime exceptions.
         * </p>
         */
        public static final int PREVIEW_SDK_INT = SystemProperties.getInt("ro.build.version.preview_sdk", 0);

        /**
         * The current development codename, or the string "REL" if this is
         * a release build.
         */
        public static final String CODENAME = getString("ro.build.version.codename");

        private static final String[] ALL_CODENAMES = getStringList("ro.build.version.all_codenames", ",");

        /**
         * @hide
         */
        public static final String[] ACTIVE_CODENAMES = "REL".equals(ALL_CODENAMES[0])
                ? new String[0] : ALL_CODENAMES;

        /**
         * The SDK version to use when accessing resources.
         * Use the current SDK version code.  For every active development codename
         * we are operating under, we bump the assumed resource platform version by 1.
         *
         * @hide
         */
        public static final int RESOURCES_SDK_INT = SDK_INT + ACTIVE_CODENAMES.length;

    }

    /**
     * Enumeration of the currently known SDK version codes.  These are the
     * values that can be found in {@link VERSION#SDK}.  Version numbers
     * increment monotonically with each official platform release.
     */
    public static class VERSION_CODES {

        /**
         * October 2008: The original, first, version of Android.  Yay!
         */
        public static final int BASE = 1;

        /**
         * February 2009: First Android update, officially called 1.1.
         */
        public static final int BASE_1_1 = 2;

        /**
         * May 2009: Android 1.5.
         */
        public static final int CUPCAKE = 3;

        /**
         * September 2009: Android 1.6.
         */
        public static final int DONUT = 4;

        /**
         * November 2009: Android 2.0
         */
        public static final int ECLAIR = 5;

        /**
         * December 2009: Android 2.0.1
         */
        public static final int ECLAIR_0_1 = 6;

        /**
         * January 2010: Android 2.1
         */
        public static final int ECLAIR_MR1 = 7;

        /**
         * June 2010: Android 2.2
         */
        public static final int FROYO = 8;

        /**
         * November 2010: Android 2.3
         */
        public static final int GINGERBREAD = 9;

        /**
         * February 2011: Android 2.3.3.
         */
        public static final int GINGERBREAD_MR1 = 10;

        /**
         * February 2011: Android 3.0.
         */
        public static final int HONEYCOMB = 11;

        /**
         * May 2011: Android 3.1.
         */
        public static final int HONEYCOMB_MR1 = 12;

        /**
         * June 2011: Android 3.2.
         */
        public static final int HONEYCOMB_MR2 = 13;

        /**
         * October 2011: Android 4.0.
         */
        public static final int ICE_CREAM_SANDWICH = 14;

        /**
         * December 2011: Android 4.0.3.
         */
        public static final int ICE_CREAM_SANDWICH_MR1 = 15;

        /**
         * June 2012: Android 4.1.
         */
        public static final int JELLY_BEAN = 16;

        /**
         * November 2012: Android 4.2, Moar jelly beans!
         */
        public static final int JELLY_BEAN_MR1 = 17;

        /**
         * July 2013: Android 4.3, the revenge of the beans.
         */
        public static final int JELLY_BEAN_MR2 = 18;

        /**
         * October 2013: Android 4.4, KitKat, another tasty treat.
         */
        public static final int KITKAT = 19;

        /**
         * June 2014: Android 4.4W. KitKat for watches, snacks on the run.
         */
        public static final int KITKAT_WATCH = 20;

        /**
         * Temporary until we completely switch to {@link #LOLLIPOP}.
         */
        public static final int L = 21;

        /**
         * November 2014: Lollipop.  A flat one with beautiful shadows.  But still tasty.
         */
        public static final int LOLLIPOP = 21;

        /**
         * March 2015: Lollipop with an extra sugar coating on the outside!
         */
        public static final int LOLLIPOP_MR1 = 22;

        /**
         * M is for Marshmallow!
         */
        public static final int M = 23;

        /**
         * N is for Nougat.
         */
        public static final int N = 24;

        /**
         * N MR1: Nougat++.
         */
        public static final int N_MR1 = 25;

        /**
         * O.
         */
        public static final int O = 26;
    }

    private static String getString(String property) {
        return SystemProperties.get(property, UNKNOWN);
    }

    private static String[] getStringList(String property, String separator) {
        String value = SystemProperties.get(property);
        if (value.isEmpty()) {
            return new String[0];
        } else {
            return value.split(separator);
        }
    }

}
