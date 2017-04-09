package wangrunz.bixbyremap;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
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
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Button button;
    private Button customButton;
    private TextView textView;
    private TextView textViewNotice;
    private TextView textViewKeyCode;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences(getString(R.string.preference_file_key),MODE_PRIVATE);
        textViewKeyCode = (TextView)findViewById(R.id.textViewKeyCode);
        textViewKeyCode.setText(String.valueOf(sharedPreferences.getInt(
                getString(R.string.source_button_id),
                Integer.valueOf(getString(R.string.bixby_button_code)))));
        textView = (TextView)findViewById(R.id.textView);
        textView.setText(sharedPreferences.getString(getString(R.string.target_name),"None"));
        textViewNotice = (TextView)findViewById(R.id.textView_notice);
        textViewNotice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        button = (Button)findViewById(R.id.button);
        customButton = (Button)findViewById(R.id.button2);
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

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(Intent.ACTION_MAIN,null);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                final List<ResolveInfo> appList = getPackageManager().queryIntentActivities(intent,0);
                PopupMenu popupMenu = new PopupMenu(MainActivity.this,button);
                popupMenu.getMenuInflater().inflate(R.menu.app_menu,popupMenu.getMenu());
                Menu menu = popupMenu.getMenu();
                menu.add(0,100,Menu.NONE,"None");
                SubMenu device_menu = menu.getItem(0).getSubMenu();

                device_menu.add(R.id.menu_device_action,101,Menu.NONE,"Recent Apps");
                device_menu.add(R.id.menu_device_action,102,Menu.NONE,"Home");
                device_menu.add(R.id.menu_device_action,103,Menu.NONE,"Back");
                device_menu.add(R.id.menu_device_action,104,Menu.NONE,"Notifications");
                device_menu.add(R.id.menu_device_action,105,Menu.NONE,"Quick Settings");
                device_menu.add(R.id.menu_device_action,106,Menu.NONE,"Split Screen");
                device_menu.add(R.id.menu_device_action,107,Menu.NONE,"Flash");
                device_menu.add(R.id.menu_device_action,108,Menu.NONE,"Ringer Mode");

                SubMenu app_menu = menu.getItem(1).getSubMenu();


                final int offset =device_menu.size()+1;
                int id = offset+100;
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
                        if (item.getItemId()<offset+100 && item.getItemId()>=100){
                            edit.putString(getString(R.string.target_name),item.getTitle().toString());
                            edit.putString(getString(R.string.target_action),item.getTitle().toString());
                            edit.putString(getString(R.string.target_activity_name),null);
                            edit.putString(getString(R.string.target_package_name),null);
                            edit.apply();
                            textView.setText(item.getTitle().toString());
                            return false;
                        }
                        ActivityInfo activityInfo = appList.get(item.getItemId()-offset-100).activityInfo;
                        String packageName = activityInfo.applicationInfo.packageName;
                        String name = activityInfo.name;
                        String label = activityInfo.loadLabel(getPackageManager()).toString();
                        textView.setText(label);
                        edit.putString(getString(R.string.target_name),label);
                        edit.putString(getString(R.string.target_action),null);
                        edit.putString(getString(R.string.target_activity_name),name);
                        edit.putString(getString(R.string.target_package_name),packageName);
                        edit.apply();
                        return false;
                    }
                });

                popupMenu.show();
            }
        });
    }

}
