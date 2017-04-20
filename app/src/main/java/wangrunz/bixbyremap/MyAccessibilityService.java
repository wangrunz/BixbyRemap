package wangrunz.bixbyremap;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import java.util.Objects;


public class MyAccessibilityService extends AccessibilityService {

    private boolean torch_status=false;
    private CameraManager cameraManager;
    private AudioManager mAudioManager;
    private NotificationManager notificationManager;
    private SharedPreferences sharedPreferences;
    private int ringer_mode;
    private long buttonEventTime;
    private boolean doubleClickTrigger = false;
    private boolean longPressTrigger = false;
    private long LONG_PRESS_INTERVAL = 1000;
    private long DOUBLE_CLICK_INTERVAL = 200;
    private Handler handler = new Handler();
    private Runnable longPressRunnable = new Runnable() {
        @Override
        public void run() {
            if (longPressTrigger){
                //Toast.makeText(MyAccessibilityService.this,"Long Press",Toast.LENGTH_SHORT).show();
                longPressTrigger=false;
                boolean isVibrate = sharedPreferences.getBoolean("vibrate", false);
                int vibrateTime = sharedPreferences.getInt("vibrate_time",1);
                action("long");
                if (isVibrate){
                    Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
                    vibrator.vibrate(vibrateTime);
                }
            }
        }
    };
    private Runnable doubleClickRunnable = new Runnable() {
        @Override
        public void run() {
            if (doubleClickTrigger){
                //Toast.makeText(MyAccessibilityService.this,"Single Click",Toast.LENGTH_SHORT).show();
                doubleClickTrigger=false;
                action("single");
            }
        }
    };

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
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        sharedPreferences = getSharedPreferences(getString(R.string.preference_file_key),MODE_PRIVATE);
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        final int action = event.getAction();
        final int keyCode = event.getKeyCode();
        int sourceKeyCode = sharedPreferences.getInt(
                getString(R.string.source_button_id),
                Integer.valueOf(getString(R.string.bixby_button_code)));
        LONG_PRESS_INTERVAL = sharedPreferences.getInt("longpressinterval",1000);
        DOUBLE_CLICK_INTERVAL = sharedPreferences.getInt("doubleclickinterval",200);
        Log.d("KeyCode",String.valueOf(keyCode)+" "+String.valueOf(action));
        if (keyCode == sourceKeyCode){

            if (action == KeyEvent.ACTION_DOWN){
                longPressTrigger=true;
                if (!doubleClickTrigger){
                    handler.postDelayed(longPressRunnable, LONG_PRESS_INTERVAL);
                }
            }
            else if (action == KeyEvent.ACTION_UP){
                handler.removeCallbacks(longPressRunnable);
                long time = event.getEventTime();
                if (longPressTrigger){
                    if (doubleClickTrigger && (time-buttonEventTime)< DOUBLE_CLICK_INTERVAL){
                        //Toast.makeText(this,"Double Click",Toast.LENGTH_SHORT).show();
                        doubleClickTrigger=false;
                        handler.removeCallbacks(doubleClickRunnable);
                        action("double");
                    }
                    else {
                        doubleClickTrigger=true;
                        handler.postDelayed(doubleClickRunnable, DOUBLE_CLICK_INTERVAL);
                    }
                }
                buttonEventTime=time;
                longPressTrigger=false;
            }
            return true;
        }
        else {
            return false;
        }
    }

    private boolean action(String button){

        String activityName = sharedPreferences.getString(getString(R.string.target_activity_name)+button, null);
        String packageName = sharedPreferences.getString(getString(R.string.target_package_name)+button, null);
        String actionName = sharedPreferences.getString(getString(R.string.target_action)+button,null);
        String targetName = sharedPreferences.getString(getString(R.string.target_name)+button,null);
        int flag = Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED;

        if (Objects.equals(actionName, "device")){
            if (targetName==null){
                return false;
            }
            switch (targetName){
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
                case "Power Dialog":
                    performGlobalAction(GLOBAL_ACTION_POWER_DIALOG);
                    break;
                case "Flash":
                    return toggleFlash();
                case "Ringer Mode":
                    changeRingerMode();
                    break;
                case "Voice Assistance":
                    startActivity(new Intent(Intent.ACTION_VOICE_COMMAND).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    break;
            }
            return true;
        }
        else if (Objects.equals(actionName, "media")){
            return mediaControl(targetName);
        }
        else if (Objects.equals(actionName, "app")) {
            return !(activityName == null || packageName == null) && startActivity(packageName, activityName, flag);
        }
        else {
            return false;
        }
    }

    private boolean mediaControl(String targetName) {
        switch (targetName){
            case "Toggle Pause":
                sendMediaButton(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
                break;
            case "Pause":
                sendMediaButton(KeyEvent.KEYCODE_MEDIA_PAUSE);
                break;
            case "Previous":
                sendMediaButton(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
                break;
            case "Next":
                sendMediaButton(KeyEvent.KEYCODE_MEDIA_NEXT);
                break;
            case "Play":
                sendMediaButton(KeyEvent.KEYCODE_MEDIA_PLAY);
                break;
            case "Stop":
                sendMediaButton(KeyEvent.KEYCODE_MEDIA_STOP);
                break;
            default:
                sendMediaButton(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
                break;
        }
        return true;
    }

    private void sendMediaButton(int keyCode) {
        KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
        mAudioManager.dispatchMediaKeyEvent(keyEvent);

        keyEvent = new KeyEvent(KeyEvent.ACTION_UP, keyCode);
        mAudioManager.dispatchMediaKeyEvent(keyEvent);
    }

    private void changeRingerMode() {
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        if (!notificationManager.isNotificationPolicyAccessGranted()){
            Toast.makeText(this,getString(R.string.notification_permission_notice),Toast.LENGTH_LONG).show();
            Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivity(intent);
        }
        else{
            ringer_mode = mAudioManager.getRingerMode();
            ringer_mode = (ringer_mode+1)%3;
            mAudioManager.setRingerMode(ringer_mode);
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
                if (cameraManager.getCameraCharacteristics(cameraId).get(CameraCharacteristics.FLASH_INFO_AVAILABLE)){
                    if (torch_status){
                        cameraManager.setTorchMode(cameraId,false);
                    }
                    else{
                        cameraManager.setTorchMode(cameraId,true);
                    }
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean startActivity(String packageName, String activityName, int flag){
        try{
            ComponentName name=new ComponentName(packageName,
                    activityName);
            Intent intent=new Intent(Intent.ACTION_MAIN);

            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setFlags(flag);
            intent.setComponent(name);

            startActivity(intent);
            return true;
        } catch (Exception e){
            Log.d("StartActivity",e.toString());
            return false;
        }
    }
}
