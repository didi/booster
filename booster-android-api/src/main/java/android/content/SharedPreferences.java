package android.content;

public interface SharedPreferences {

    public interface Editor {

        void apply();

        boolean commit();
    }

}
