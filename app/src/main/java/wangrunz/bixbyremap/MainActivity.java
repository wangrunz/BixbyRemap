package wangrunz.bixbyremap;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Button button;
    private TextView textView;
    private TextView textViewNotice;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences(getString(R.string.preference_file_key),MODE_PRIVATE);
        textView = (TextView)findViewById(R.id.textView);
        textView.setText(sharedPreferences.getString(getString(R.string.target_package_name),"N/A"));
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
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(Intent.ACTION_MAIN,null);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                final List<ResolveInfo> appList = getPackageManager().queryIntentActivities(intent,0);
                PopupMenu popupMenu = new PopupMenu(MainActivity.this,button);
                popupMenu.getMenuInflater().inflate(R.menu.app_menu,popupMenu.getMenu());
                Menu menu = popupMenu.getMenu();
                menu.add(0,0,Menu.NONE,R.string.none);
                int count =1;
                for (ResolveInfo info: appList){
                    menu.add(0,count,Menu.NONE,info.loadLabel(getPackageManager())).setIcon(info.loadIcon(getPackageManager()));
                    count++;
                }
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        SharedPreferences.Editor edit = sharedPreferences.edit();
                        if (item.getItemId()==0){
                            edit.putString(getString(R.string.target_activity_name),null);
                            edit.putString(getString(R.string.target_package_name),null);
                            edit.apply();
                            textView.setText("N/A");
                            return false;
                        }
                        ActivityInfo activityInfo = appList.get(item.getItemId()-1).activityInfo;
                        String packageName = activityInfo.applicationInfo.packageName;
                        String name = activityInfo.name;
                        textView.setText(activityInfo.packageName);
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
