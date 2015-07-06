package com.junjunguo.pocketmaps.controller;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.model.map.AndroidDownloader;
import com.junjunguo.pocketmaps.model.map.DownloadFiles;
import com.junjunguo.pocketmaps.model.util.MyDownloadAdapter;
import com.junjunguo.pocketmaps.model.util.MyMap;
import com.junjunguo.pocketmaps.model.util.RVItemTouchListener;
import com.junjunguo.pocketmaps.model.util.SetStatusBarColor;
import com.junjunguo.pocketmaps.model.util.Variable;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DownloadMapActivity extends AppCompatActivity {
    private MyDownloadAdapter myDownloadAdapter;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //         set status bar
        new SetStatusBarColor().setStatusBarColor(findViewById(R.id.statusBarBackgroundDownload),
                getResources().getColor(R.color.my_primary_dark), this);

        downloadList();
        activeRecyclerView(new ArrayList());
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * download and generate a list of countries from server
     */
    private void downloadList() {
        new AsyncTask<URL, Integer, List<MyMap>>() {
            @Override protected List doInBackground(URL... params) {
                String[] lines = new String[0];
                try {
                    lines = new AndroidDownloader().downloadAsString(Variable.getVariable().getFileListURL())
                            .split("\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                List<MyMap> myMaps = new ArrayList<>();
                for (String str : lines) {
                    int index = str.indexOf("href=\"");
                    if (index >= 0) {
                        index += 6;
                        int lastIndex = str.indexOf(".ghz", index);
                        if (lastIndex >= 0) {
                            int sindex = str.indexOf("right\">", str.length() - 52);
                            int slindex = str.indexOf("M", sindex);
                            String mapName = str.substring(index, lastIndex);
                            boolean downloaded = Variable.getVariable().getLocalMapNameList().contains(mapName);
                            log("downloaded: " + downloaded);
                            if (sindex >= 0 && slindex >= 0) {
                                myMaps.add(new MyMap(mapName, str.substring(sindex + 7, slindex + 1), downloaded));
                            } else {
                                myMaps.add(new MyMap(str.substring(index, lastIndex), "", downloaded));
                            }
                        }
                    }
                }
                return myMaps;
            }

            @Override protected void onPostExecute(List<MyMap> myMaps) {
                super.onPostExecute(myMaps);
                listReady(myMaps);
            }
        }.execute();
    }


    /**
     * list of countries are ready
     *
     * @param myMaps
     */
    private void listReady(List<MyMap> myMaps) {
        if (myMaps.isEmpty()) {
            Toast.makeText(this, "There is a problem with the server, please report this to app developer!",
                    Toast.LENGTH_SHORT).show();
        } else {
            log(myMaps.toString());
            myDownloadAdapter.addAll(myMaps);
        }
    }

    /**
     * active directions, and directions view
     */
    private void activeRecyclerView(List myMaps) {
        RecyclerView mapsRV;
        RecyclerView.LayoutManager layoutManager;

        mapsRV = (RecyclerView) findViewById(R.id.my_maps_download_recycler_view);
        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setAddDuration(2000);
        animator.setRemoveDuration(2000);
        mapsRV.setItemAnimator(animator);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mapsRV.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        mapsRV.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        myDownloadAdapter = new MyDownloadAdapter(myMaps);
        mapsRV.setAdapter(myDownloadAdapter);
        onItemTouchHandler(mapsRV);
    }

    /**
     * perform actions when item touched
     *
     * @param mapsRV
     */
    private void onItemTouchHandler(RecyclerView mapsRV) {
        mapsRV.addOnItemTouchListener(new RVItemTouchListener(new RVItemTouchListener.OnItemTouchListener() {
            public boolean onItemTouch(View view, int position, MotionEvent e) {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        view.setBackgroundColor(getResources().getColor(R.color.my_primary_light));
                        return true;
                    case MotionEvent.ACTION_UP:
                        view.setBackgroundColor(getResources().getColor(R.color.my_icons));
                        activeDownload(view, position);
                        return true;
                }
                return false;
            }
        }));
    }

    /**
     * download map
     *
     * @param view
     * @param position
     */
    private void activeDownload(View view, int position) {
        Variable.getVariable().setPrepareInProgress(true);
        MyMap myMap = myDownloadAdapter.getItem(position);
        if (!myMap.isDownloaded()) {
            Variable.getVariable().setCountry(myMap.getMapName());
            new DownloadFiles(Variable.getVariable().getMapsFolder(), Variable.getVariable().getCountry(),
                    myMap.getUrl(), this);
        }
    }

    private void log(String s) {
        System.out.println(this.getClass().getSimpleName() + "-------------------" + s);
    }

}