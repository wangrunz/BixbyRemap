package wangrunz.bixbyremap;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Instrumentation;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.BaseInputConnection;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by wrz19 on 4/8/2017.
 */

public class MyAccessibilityService extends AccessibilityService {

    private static final List<Integer> keyCodeList = Arrays.asList(24,25);
    private boolean torch_status=false;
    private CameraManager cameraManager;
    private AudioManager audioManager;
    private NotificationManager notificationManager;
    private int ringer_mode;

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
        Log.d("KeyCode",String.valueOf(keyCode)+" "+String.valueOf(action));
        if (action == KeyEvent.ACTION_DOWN){
            SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.preference_file_key),MODE_PRIVATE);
            int sourceKeyCode = sharedPreferences.getInt(
                    getString(R.string.source_button_id),
                    Integer.valueOf(getString(R.string.bixby_button_code)));
            if (!keyCodeList.contains(sourceKeyCode)){
                sourceKeyCode=Integer.valueOf(getString(R.string.bixby_button_code));
            }
            if (keyCode == sourceKeyCode){

                String activityName = sharedPreferences.getString(getString(R.string.target_activity_name), null);
                String packageName = sharedPreferences.getString(getString(R.string.target_package_name), null);
                String actionName = sharedPreferences.getString(getString(R.string.target_action),null);
                if (actionName!=null){
                    switch (actionName){
                        case "None":
                            return false;
                        case "Recent Apps":
                            performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
                            break;
                        case "Home":
                            performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
                            break;
                        case "Back":
                            performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                            break;
                        case "Notifications":
                            performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS);
                            break;
                        case "Quick Settings":
                            performGlobalAction(AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS);
                            break;
                        case "Split Screen":
                            performGlobalAction(GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN);
                            break;
                        case "Flash":
                            return toggleFlash();
                        case "Ringer Mode":
                            changeRingerMode();
                            break;
                    }
                    return true;
                }
                if (activityName==null || packageName==null){
                    return false;
                }
                return startActivity(packageName,activityName);
            }
        }
        return false;
    }

    private void changeRingerMode() {
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        if (!notificationManager.isNotificationPolicyAccessGranted()){
            Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivity(intent);
        }
        else{
            audioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
            ringer_mode = audioManager.getRingerMode();
            ringer_mode = (ringer_mode+1)%3;
            audioManager.setRingerMode(ringer_mode);
        }
    }

    private boolean toggleFlash() {
        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        cameraManager.registerTorchCallback(new CameraManager.TorchCallback() {
            @Override
            public void onTorchModeChanged(@NonNull String cameraId, boolean enabled) {
                super.onTorchModeChanged(cameraId, enabled);
                torch_status=enabled;
            }
        },null);

        try {
            for (String cameraId: cameraManager.getCameraIdList()){
                if (cameraManager.getCameraCharacteristics(cameraId).get(CameraCharacteristics.FLASH_INFO_AVAILABLE).booleanValue()){
                    if (torch_status){
                        cameraManager.setTorchMode(cameraId,false);
                    }
                    else{
                        cameraManager.setTorchMode(cameraId,true);
                    }
                }
            }
            return true;
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return false;
        }
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
