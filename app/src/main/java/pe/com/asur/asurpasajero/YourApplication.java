package pe.com.asur.asurpasajero;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

/**
 * Created by agus on 15/03/2018.
 */

public class YourApplication extends Application {

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        MultiDex.install(this);
    }
    @Override
    public void onCreate() {
        super.onCreate();


    }
}