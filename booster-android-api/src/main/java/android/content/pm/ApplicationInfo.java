package android.content.pm;

import android.os.Parcelable;

public class ApplicationInfo extends PackageItemInfo implements Parcelable {

    public static int FLAG_SYSTEM                   = 0x00000001;
    public static int FLAG_DEBUGGABLE               = 0x00000002;
    public static int FLAG_HAS_CODE                 = 0x00000004;
    public static int FLAG_PERSISTENT               = 0x00000008;

    public static int FLAG_FACTORY_TEST             = 0x00000010;
    public static int FLAG_ALLOW_TASK_REPARENTING   = 0x00000020;
    public static int FLAG_ALLOW_CLEAR_USER_DATA    = 0x00000040;
    public static int FLAG_UPDATED_SYSTEM_APP       = 0x00000080;

    public static int FLAG_TEST_ONLY                = 0x00000100;
    public static int FLAG_SUPPORTS_SMALL_SCREENS   = 0x00000200;
    public static int FLAG_SUPPORTS_NORMAL_SCREENS  = 0x00000400;
    public static int FLAG_SUPPORTS_LARGE_SCREENS   = 0x00000800;

    public static int FLAG_RESIZEABLE_FOR_SCREENS   = 0x00001000;
    public static int FLAG_SUPPORTS_SCREEN_DENSITIES= 0x00002000;
    public static int FLAG_VM_SAFE_MODE             = 0x00004000;
    public static int FLAG_ALLOW_BACKUP             = 0x00008000;

    public static int FLAG_KILL_AFTER_RESTORE       = 0x00010000;
    public static int FLAG_RESTORE_ANY_VERSION      = 0x00020000;
    public static int FLAG_EXTERNAL_STORAGE         = 0x00040000;
    public static int FLAG_SUPPORTS_XLARGE_SCREENS  = 0x00080000;

    public String className;

    public int descriptionRes;

    public String dataDir;

    public boolean enabled;

    public int flags;

    public String manageSpaceActivityName;

    public String nativeLibraryDir;

    public String permission;

    public String processName;

    public String publicSourceDir;

    public String[] sharedLibraryFiles;

    public String sourceDir;

    public int targetSdkVersion;

    public String taskAffinity;

    public int theme;

    public int uid;

    public int describeContents () {
        throw new RuntimeException("Stub!");
    }

    public CharSequence loadDescription (PackageManager pm) {
        throw new RuntimeException("Stub!");
    }

}
