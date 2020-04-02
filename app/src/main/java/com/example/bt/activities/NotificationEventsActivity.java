package com.example.bt.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ListView;

import com.example.bt.list_adapters.AllAppsListAdapder;
import com.example.bt.list_adapters.AllAppsRowItem;
import com.example.bt.R;

import java.util.ArrayList;
import java.util.List;

public class NotificationEventsActivity extends AppCompatActivity {

    private ListView allAppsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_events);

        // set up fields
        InitializeFields();

        PackageManager pm = getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        ArrayList<AllAppsRowItem> allAppList = new ArrayList<>();
        for(ApplicationInfo info : packages){
            if((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0){
                AllAppsRowItem allAppListRow = new AllAppsRowItem();
                allAppListRow.setName(info.loadLabel(pm).toString());
                allAppListRow.setIcon(info.loadIcon(pm));
                allAppList.add(allAppListRow);
            }
        }
        AllAppsListAdapder allAppsListAdapder = new AllAppsListAdapder(getApplicationContext(), allAppList);
        allAppsListView.setAdapter(allAppsListAdapder);
    }

    private void InitializeFields() {
        allAppsListView = findViewById(R.id.allAppsListView);
    }
}
