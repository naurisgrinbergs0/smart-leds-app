package com.example.bt.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.bt.MemoryConnector;
import com.example.bt.dialogs.ChooseColorDialog;
import com.example.bt.list_adapters.AllAppsListAdapder;
import com.example.bt.list_adapters.AllAppsRowItem;
import com.example.bt.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationEventsActivity extends AppCompatActivity {

    private ListView allAppsListView;
    private View backLayout;
    private ArrayList<AllAppsRowItem> allAppList;
    private ChooseColorDialog chooseColorDialog;
    JSONObject appColors;
    AllAppsListAdapder allAppsListAdapder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_events);

        // set up fields
        InitializeFields();

        setEventListeners();

        // set up the list adapter
        List<ApplicationInfo> packages = getInstalledApps(this);
        for(ApplicationInfo info : packages){
            if((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0){
                PackageManager pm = getPackageManager();
                AllAppsRowItem allAppListRow = new AllAppsRowItem();
                allAppListRow.setName(info.loadLabel(pm).toString());
                allAppListRow.setIcon(info.loadIcon(pm));
                allAppListRow.setPackageName(info.packageName);
                // load color - if exists
                if(appColors.has(info.packageName)) {
                    try {
                        allAppListRow.setColor(appColors.getInt(info.packageName));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else
                    allAppListRow.setColor(Color.TRANSPARENT);

                allAppList.add(allAppListRow);
            }
        }

        allAppsListAdapder = new AllAppsListAdapder(getApplicationContext(), allAppList);
        allAppsListView.setAdapter(allAppsListAdapder);
    }

    private void goBack() {
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private JSONObject loadAppColors() {
        JSONObject j = MemoryConnector.readJsonFromFile(this, getString(R.string.file_name));
        if(j == null)
            j = new JSONObject();
        return j;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goBack();
    }

    private void InitializeFields() {
        allAppsListView = findViewById(R.id.allAppsListView);
        allAppList = new ArrayList<>();
        backLayout = findViewById(R.id.backConstraintLayout);
        chooseColorDialog = new ChooseColorDialog();
        appColors = loadAppColors();
    }

    private void setEventListeners(){
        backLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack();
            }
        });

        allAppsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // open dialog
                FragmentManager fm = NotificationEventsActivity.this.getSupportFragmentManager();
                chooseColorDialog.packageName = allAppList.get(position).getPackageName();
                chooseColorDialog.jsonObject = appColors;
                chooseColorDialog.listItem = allAppsListAdapder.getItem(position);
                chooseColorDialog.listItemView = view;
                chooseColorDialog.show(fm, "fragment_choose_color");
            }
        });
    }

    public static List<ApplicationInfo> getInstalledApps(Context context) {
        PackageManager packageManager = context.getPackageManager();
        List<ApplicationInfo> apps = packageManager.getInstalledApplications(0);
        Collections.sort(apps, new ApplicationInfo.DisplayNameComparator(packageManager));
        return apps;
    }
}
