package android.content.pm;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

public class PackageItemInfo {

    public int banner;

    public int icon;

    public int labelRes;

    public int logo;

    public Bundle metaData;

    public String name;

    public CharSequence nonLocalizedLabel;

    public String packageName;

    public Drawable loadIcon (PackageManager pm) {
        throw new RuntimeException("Stub!");
    }


}
