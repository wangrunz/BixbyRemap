package wangrunz.bixbyremap;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by wrz19 on 4/8/2017.
 */

public class MyAccessibilityService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d("AccessibilityEvent",event.toString());
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    public void onServiceConnected(){
        AccessibilityServiceInfo info = getServiceInfo();
        info.flags = AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS;
        setServiceInfo(info);
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        if (action == KeyEvent.ACTION_DOWN){
            SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.preference_file_key),MODE_PRIVATE);
            if (keyCode == 1082){

                String activityName = sharedPreferences.getString(getString(R.string.target_activity_name), null);
                String packageName = sharedPreferences.getString(getString(R.string.target_package_name), null);
                if (activityName==null || packageName==null){
                    return false;
                }
                return startActivity(packageName,activityName);
            }
        }
        return false;
    }

    private boolean startActivity(String packageName, String activityName){
        try{
            ComponentName name=new ComponentName(packageName,
                    activityName);
            Intent intent=new Intent(Intent.ACTION_MAIN);

            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            intent.setComponent(name);

            startActivity(intent);
            return true;
        } catch (Exception e){
            return false;
        }
    }
}
