package wangrunz.bixbyremap;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Button SingleClickMenuButton;
    private TextView SingleClickAction;
    private Button DoubleClickMenuButton;
    private TextView DoubleClickAction;
    private Button LongPressMenuButton;
    private TextView LongPressAction;
    private Button customButton;
    private TextView textViewNotice;
    private TextView textViewKeyCode;
    private SharedPreferences sharedPreferences;
    private SeekBar LongPressSeekBar;
    private SeekBar DoubleClickSeekBar;
    private TextView LongPressInt;
    private TextView DoubleClickInt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences(getString(R.string.preference_file_key),MODE_PRIVATE);
        runUpdatesIfNecessary();
        textViewKeyCode = (TextView)findViewById(R.id.KeyCode);
        textViewKeyCode.setText(String.valueOf(sharedPreferences.getInt(
                getString(R.string.source_button_id),
                Integer.valueOf(getString(R.string.bixby_button_code)))));
        SingleClickAction = (TextView)findViewById(R.id.SingleClickAction);
        SingleClickAction.setText(sharedPreferences.getString(getString(R.string.target_name)+"single","None"));
        SingleClickMenuButton = (Button)findViewById(R.id.SingleClickMenu);

        DoubleClickAction = (TextView)findViewById(R.id.DoubleClickAction);
        DoubleClickAction.setText(sharedPreferences.getString(getString(R.string.target_name)+"double","None"));
        DoubleClickMenuButton = (Button)findViewById(R.id.DoubleClickMenu);

        LongPressAction = (TextView)findViewById(R.id.LongPressAction);
        LongPressAction.setText(sharedPreferences.getString(getString(R.string.target_name)+"long","None"));
        LongPressMenuButton = (Button)findViewById(R.id.LongPressMenu);

        textViewNotice = (TextView)findViewById(R.id.textView_notice);
        textViewNotice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        customButton = (Button)findViewById(R.id.KeyCodeCustom);
        customButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                        .setMessage("Press Custom Key")
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //
                            }
                        }).create();
                dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        textViewKeyCode.setText(String.valueOf(keyCode));
                        SharedPreferences.Editor edit = sharedPreferences.edit();
                        edit.putInt(getString(R.string.source_button_id),keyCode);
                        edit.apply();
                        dialog.dismiss();
                        return true;
                    }
                });
                dialog.show();
            }
        });

        SingleClickMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(SingleClickMenuButton);
            }
        });

        DoubleClickMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(DoubleClickMenuButton);
            }
        });


        LongPressMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(LongPressMenuButton);
            }
        });


        LongPressSeekBar = (SeekBar)findViewById(R.id.LongPressSeekBar);
        DoubleClickSeekBar = (SeekBar)findViewById(R.id.DoubleClickSeekBar);
        LongPressInt = (TextView)findViewById(R.id.LongPressInt);
        DoubleClickInt = (TextView)findViewById(R.id.DoubleClickInt);
        LongPressSeekBar.setProgress(sharedPreferences.getInt("longpressinterval",1000));
        DoubleClickSeekBar.setProgress(sharedPreferences.getInt("doubleclickinterval",200));
        LongPressInt.setText(String.valueOf(LongPressSeekBar.getProgress()));
        DoubleClickInt.setText(String.valueOf(DoubleClickSeekBar.getProgress()));
        LongPressSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue=progress;
                LongPressInt.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("longpressinterval",progressChangedValue);
                editor.apply();
            }
        });
        DoubleClickSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue=progress;
                DoubleClickInt.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("doubleclickinterval",progressChangedValue);
                editor.apply();
            }
        });
    }

    public void showPopupMenu(Button button){

        final String key;
        final TextView textView;
        switch(button.getId()){
            case R.id.SingleClickMenu:
                key = "single";
                textView = SingleClickAction;
                break;
            case R.id.DoubleClickMenu:
                key = "double";
                textView = DoubleClickAction;
                break;
            case R.id.LongPressMenu:
                key = "long";
                textView = LongPressAction;
                break;
            default:
                key = "single";
                textView = SingleClickAction;
                break;
        }


        PopupMenu popupMenu = new PopupMenu(MainActivity.this, button);
        popupMenu.getMenuInflater().inflate(R.menu.app_menu,popupMenu.getMenu());
        Menu menu = popupMenu.getMenu();
        menu.add(0,100,Menu.NONE,"None");

        SubMenu device_menu = menu.getItem(0).getSubMenu();

        device_menu.add(R.id.menu_device_action,100,Menu.NONE,"Recent Apps");
        device_menu.add(R.id.menu_device_action,101,Menu.NONE,"Home");
        device_menu.add(R.id.menu_device_action,102,Menu.NONE,"Back");
        device_menu.add(R.id.menu_device_action,103,Menu.NONE,"Notifications");
        device_menu.add(R.id.menu_device_action,104,Menu.NONE,"Quick Settings");
        device_menu.add(R.id.menu_device_action,105,Menu.NONE,"Split Screen");
        device_menu.add(R.id.menu_device_action,106,Menu.NONE,"Power Dialog");
        device_menu.add(R.id.menu_device_action,107,Menu.NONE,"Flash");
        device_menu.add(R.id.menu_device_action,108,Menu.NONE,"Ringer Mode");
        device_menu.add(R.id.menu_device_action,109,Menu.NONE,"Voice Assistance");


        SubMenu media_menu = menu.getItem(1).getSubMenu();

        media_menu.add(R.id.menu_media_controller,200,Menu.NONE,"Toggle Pause");
        media_menu.add(R.id.menu_media_controller,201,Menu.NONE,"Play");
        media_menu.add(R.id.menu_media_controller,202,Menu.NONE,"Pause");
        media_menu.add(R.id.menu_media_controller,203,Menu.NONE,"Stop");
        media_menu.add(R.id.menu_media_controller,204,Menu.NONE,"Previous");
        media_menu.add(R.id.menu_media_controller,205,Menu.NONE,"Next");

        SubMenu app_menu = menu.getItem(2).getSubMenu();

        final Intent intent = new Intent(Intent.ACTION_MAIN,null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        final List<ResolveInfo> appList = getPackageManager().queryIntentActivities(intent,0);
        Collections.sort(appList, new ResolveInfo.DisplayNameComparator(getPackageManager()));

        int id = 900;
        for (ResolveInfo info: appList){
            app_menu.add(R.id.menu_app_list,id,Menu.NONE,info.loadLabel(getPackageManager()));
            id++;
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                SharedPreferences.Editor edit = sharedPreferences.edit();
                if (item.getItemId()<100){
                    return false;
                }

                else if (item.getItemId()<200){
                    edit.putString(getString(R.string.target_name)+key,item.getTitle().toString());
                    edit.putString(getString(R.string.target_action)+key,"device");
                    edit.putString(getString(R.string.target_activity_name)+key,null);
                    edit.putString(getString(R.string.target_package_name)+key,null);
                    edit.apply();
                    textView.setText(item.getTitle().toString());
                    return false;
                }
                else if (item.getItemId()<300){
                    edit.putString(getString(R.string.target_name)+key,item.getTitle().toString());
                    edit.putString(getString(R.string.target_action)+key,"media");
                    edit.putString(getString(R.string.target_activity_name)+key,null);
                    edit.putString(getString(R.string.target_package_name)+key,null);
                    edit.apply();
                    textView.setText(item.getTitle().toString());
                    return false;
                }
                else if (item.getItemId()>=900){
                    ActivityInfo activityInfo = appList.get(item.getItemId()-900).activityInfo;
                    String packageName = activityInfo.applicationInfo.packageName;
                    String name = activityInfo.name;
                    String label = activityInfo.loadLabel(getPackageManager()).toString();
                    textView.setText(label);
                    edit.putString(getString(R.string.target_name)+key,label);
                    edit.putString(getString(R.string.target_action)+key,"app");
                    edit.putString(getString(R.string.target_activity_name)+key,name);
                    edit.putString(getString(R.string.target_package_name)+key,packageName);
                    edit.apply();
                    return false;
                }
                return false;
            }
        });

        popupMenu.show();
    }

    void runUpdatesIfNecessary() {
        try {
            int versionCode = getPackageManager().getPackageInfo(getPackageName(),0).versionCode;
            if (sharedPreferences.getInt("lastUpdate", 0) != versionCode) {
                try {
                    runUpdates();
                    // Commiting in the preferences, that the update was successful.
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("lastUpdate", versionCode);
                    editor.apply();
                } catch(Throwable t) {
                    t.printStackTrace();
                }
            }
        }catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }
    }

    private void runUpdates() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
