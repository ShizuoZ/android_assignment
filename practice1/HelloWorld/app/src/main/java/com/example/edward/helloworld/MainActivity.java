package com.example.edward.helloworld;

import android.app.Activity;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.net.Uri;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;

public class MainActivity extends Activity {
    private Button button;
    private Button logreceiver;
    private EditText fileurl;
    private TextView logtext;
    private RadioButton ruwifi;
    private RadioButton anywifi;
    private RadioGroup wifiselect;
    private WifiManager wifimanager;
    private WifiInfo wifiinfo;
    private DownloadManager downloadManager;
    private ProgressBar downloadProgress;
    private long downloadId=0;
    private long connectsecond=0;
    private long dlbgn_second=0;
    private long dlend_second=0;
    private long clkbgn_second=0;

    private String connecttime;

    String Url;
    String ssid;
    String connectduration;
    int fileSize;
    public static final String DOWNLOAD_FOLDER_NAME = "Download";
    public static final String DOWNLOAD_FILE_NAME = "gesturesecurity.pdf";

    private TextView downloadSize;
    private TextView downloadPrecent;
    private MyHandler              handler;
    private DownloadChangeObserver downloadObserver;
    private CompleteReceiver       completeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button) findViewById(R.id.btnProgressBar);
        logreceiver = (Button) findViewById(R.id.log);
        fileurl = (EditText) findViewById(R.id.url);
        downloadSize = (TextView) findViewById(R.id.progressSize);
        downloadPrecent = (TextView) findViewById(R.id.progressPercent);
        logtext = (TextView) findViewById(R.id.logtext);
        ruwifi = (RadioButton) findViewById(R.id.radioButton);
        anywifi = (RadioButton) findViewById(R.id.radioButton2);
        wifiselect = (RadioGroup) findViewById(R.id.radiogroup);
        downloadProgress = (ProgressBar) findViewById(R.id.progressBar);
        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        wifimanager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        logreceiver.setOnClickListener(loglistener);
        button.setOnClickListener(downloadListener);
        wifiselect.setOnCheckedChangeListener(radioboxlistener);
        downloadObserver = new DownloadChangeObserver();
        completeReceiver = new CompleteReceiver();
        handler = new MyHandler();
        registerReceiver(wifireceiver, new IntentFilter(wifimanager.WIFI_STATE_CHANGED_ACTION));
        registerReceiver(completeReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    private OnClickListener downloadListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            wifiinfo = wifimanager.getConnectionInfo();
            ssid = wifiinfo.getSSID();
            if (anywifi.isChecked()){
                clkbgn_second=System.currentTimeMillis();
                downloadTask();
            } else if ((ruwifi.isChecked()) && ((ssid.equals("\"LAWN\"")) || (ssid.equals("\"ECE\""))
                    || (ssid.equals("\"RUWireless_Secure\""))
                    || (ssid.equals("\"RUWireless\"")))){
                clkbgn_second=System.currentTimeMillis();
                downloadTask();
            }
        }
    };

    private OnClickListener loglistener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            String logword;
            String downloadtime;
            String latency;
            if (wifimanager.isWifiEnabled()) {
                long currentsecond = System.currentTimeMillis();
                fileSize = getBytesAndStatus(downloadId)[1];
                String filesizestring = String.format("%.2f", (fileSize) / 1024f) + "Kb" ;
                String downloadSpeed = String.format("%.3f",(fileSize/((dlend_second - dlbgn_second) / 1000f))) + "Byte/sec" ;
                connectduration = String.format("%.3f",(currentsecond - connectsecond) / 1000f) + "seconds";
                downloadtime = String.format("%.3f", (dlend_second - dlbgn_second) / 1000f) + "seconds";
                latency = String.format("%.3f", (dlbgn_second-clkbgn_second) / 1000f) + "seconds";
                wifiinfo = wifimanager.getConnectionInfo();
                ssid = wifiinfo.getSSID();
                logword = "Connection Time: " + connecttime + "\n" +
                        ssid + "\n" + "Connection duration: " + connectduration + "\n"
                        + "latency: " + latency + "\n"
                        + "total download time: " + downloadtime + "\n"
                        + "file size: " + filesizestring + "\n"
                        + "download speed: " + downloadSpeed + "\n";
                logtext.setText(logword);
            }
            else {
                logword = "Connection wifi: NULL";
                logtext.setText(logword);
            }
            Log.i("log","output log");
            String path = Environment.getExternalStorageDirectory().getPath();
            File dir = new File( path + "/Download");
            if(!dir.exists()){
                dir.mkdir();
            }
            try{
                File file = new File( path + "/Download" + "/log.txt");
                FileOutputStream fout = new FileOutputStream(file,true);
                fout.write(logword.getBytes());
                fout.close();
            }catch (FileNotFoundException e){
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    };

    private OnCheckedChangeListener radioboxlistener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
        }
    };

    private BroadcastReceiver wifireceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                int wifistate = intent.getIntExtra(WifiManager.EXTRA_WIFI_INFO, WifiManager.WIFI_STATE_ENABLED);
                if (wifistate == WifiManager.WIFI_STATE_ENABLED) {
                    long time = System.currentTimeMillis();
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(time);
                    connecttime = ( 1 + calendar.get(Calendar.MONTH)) + "/" + calendar.get(Calendar.DATE)
                            + "/" + (calendar.get(Calendar.YEAR)) + " " + calendar.get(Calendar.HOUR_OF_DAY)
                            + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND);
                    connectsecond = System.currentTimeMillis();
                }
            }
        }
    };

    private void downloadTask(){
        Url = fileurl.getText().toString();
        String filename[] = Url.split("/");
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(Url));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        request.setDestinationInExternalPublicDir(DOWNLOAD_FOLDER_NAME, filename[filename.length-1]);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
        request.setVisibleInDownloadsUi(false);
        downloadId = downloadManager.enqueue(request);
    }

    class DownloadChangeObserver extends ContentObserver {

        public DownloadChangeObserver() {
            super(handler);
        }
            @Override
            public void onChange (boolean selfChange) {
                updateView();
            }

    }

    class CompleteReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            long completeDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (completeDownloadId == downloadId) {
                }
            dlend_second=System.currentTimeMillis();
            }
    };

    public void updateView() {
        int[] bytesAndStatus = getBytesAndStatus(downloadId);
        handler.sendMessage(handler.obtainMessage(0, bytesAndStatus[0], bytesAndStatus[1], bytesAndStatus[2]));
    }

    @Override
    protected void onResume() {
        super.onResume();
        /** observer download change **/
        getContentResolver().registerContentObserver(Uri.parse("content://downloads/"), true, downloadObserver);
        updateView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        getContentResolver().unregisterContentObserver(downloadObserver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(completeReceiver);
    }

    private class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    int status = (Integer)msg.obj;
                    if (isDownloading(status)) {
                        downloadProgress.setVisibility(View.VISIBLE);
                        downloadProgress.setMax(0);
                        downloadProgress.setProgress(0);
                        downloadSize.setVisibility(View.VISIBLE);
                        downloadPrecent.setVisibility(View.VISIBLE);

                        if (msg.arg2 < 0) {
                            downloadProgress.setIndeterminate(true);
                            downloadPrecent.setText("0%");
                            downloadSize.setText("0M/0M");
                        } else {
                            downloadProgress.setIndeterminate(false);
                            downloadProgress.setMax(msg.arg2);
                            downloadProgress.setProgress(msg.arg1);
                            downloadPrecent.setText(getNotiPercent(msg.arg1, msg.arg2));
                            downloadSize.setText(getAppSize(msg.arg1) + "/" + getAppSize(msg.arg2));
                        }
                    } else {
                        downloadProgress.setVisibility(View.GONE);
                        downloadProgress.setMax(0);
                        downloadProgress.setProgress(0);
                        button.setVisibility(View.VISIBLE);
                        downloadSize.setVisibility(View.GONE);
                        downloadPrecent.setVisibility(View.GONE);
                    }
                    break;
            }
        }
    }

    static final DecimalFormat DOUBLE_DECIMAL_FORMAT = new DecimalFormat("0.##");

    public static final int    MB_2_BYTE             = 1024 * 1024;
    public static final int    KB_2_BYTE             = 1024;

    public static CharSequence getAppSize(long size) {
        if (size <= 0) {
            return "0M";
        }

        if (size >= MB_2_BYTE) {
            return new StringBuilder(16).append(DOUBLE_DECIMAL_FORMAT.format((double)size / MB_2_BYTE)).append("M");
        } else if (size >= KB_2_BYTE) {
            return new StringBuilder(16).append(DOUBLE_DECIMAL_FORMAT.format((double)size / KB_2_BYTE)).append("K");
        } else {
            return size + "B";
        }
    }

    public static String getNotiPercent(long progress, long max) {
        int rate = 0;
        if (progress <= 0 || max <= 0) {
            rate = 0;
        } else if (progress > max) {
            rate = 100;
        } else {
            rate = (int)((double)progress / max * 100);
        }
        return new StringBuilder(16).append(rate).append("%").toString();
    }

    public static boolean isDownloading(int downloadManagerStatus) {
        return downloadManagerStatus == DownloadManager.STATUS_RUNNING
                || downloadManagerStatus == DownloadManager.STATUS_PAUSED
                || downloadManagerStatus == DownloadManager.STATUS_PENDING;
    }

    public int[] getBytesAndStatus(long downloadId) {
        int[] bytesAndStatus = new int[] {-1, -1, 0};
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        Cursor c = null;
        try {
            c = downloadManager.query(query);
            if (c != null && c.moveToFirst()) {
                bytesAndStatus[0] = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                while(bytesAndStatus[0]<=0){dlbgn_second=System.currentTimeMillis();break;}
                bytesAndStatus[1] = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                bytesAndStatus[2] = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return bytesAndStatus;
    }
}