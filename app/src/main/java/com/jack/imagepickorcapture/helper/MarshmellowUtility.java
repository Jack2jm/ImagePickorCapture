package com.jack.imagepickorcapture.helper;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/*
 * Created by Jatin Mandanka on 17/12/16.
 */

public class MarshmellowUtility {
    public static final int MY_PERMISSIONS_REQUEST_PERMISSIONS = 123;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static int checkPermission(final Context context, String permisssion) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if(currentAPIVersion>= Build.VERSION_CODES.M)
        {
            if (ContextCompat.checkSelfPermission(context, permisssion) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context,permisssion)) {
                    ActivityCompat.requestPermissions((Activity)context, new String[]{permisssion}, MY_PERMISSIONS_REQUEST_PERMISSIONS);
                } else {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{permisssion}, MY_PERMISSIONS_REQUEST_PERMISSIONS);
                }
                return 2;
            } else {
                return 1;
            }
        } else {
            return 0;
        }
    }
}
/*
0	not marshmellow user
1	granted + granted already
2	dialog open   + Never ask again
3	granted
4	Deny,Never ask again
*/