package com.example.memorytracker;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatRadioButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class MainActivity extends Activity {

    int interval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Handler handler = new Handler();
        final Runnable[] runnable = new Runnable[1];
        final AppCompatEditText inputInterval = findViewById(R.id.setInterval);
        final Button button1 = findViewById(R.id.start_btn);
        final Button button2 = findViewById(R.id.stop_btn);
        final Button button3 = findViewById(R.id.set_btn);
        final Button button4 = findViewById(R.id.reset_btn);
        final AppCompatRadioButton radio2 = findViewById(R.id.no);
        final TextView intervalText = findViewById(R.id.interval);

        radio2.setChecked(true);

        RadioGroup rg = findViewById(R.id.radio_group);
        rg.setOnCheckedChangeListener((group, checkedId) -> {
            if(checkedId == R.id.yes) {
                inputInterval.setFocusableInTouchMode(true);
                inputInterval.setEnabled(true);
                inputInterval.setFocusable(true);
                inputInterval.setTextColor(Color.rgb(255,255,255));
                inputInterval.setHintTextColor(Color.rgb(255,255,255));
            }
            else if (checkedId == R.id.no) {
                inputInterval.setEnabled(false);
                inputInterval.setFocusable(false);
                interval = 0;
                String intervalTxt = "Current Interval: " + interval + " seconds";
                intervalText.setText(intervalTxt);
                inputInterval.setTextColor(Color.rgb(128,128,128));
                inputInterval.setHintTextColor(Color.rgb(128,128,128));
            }
        });


        button1.setOnClickListener(v -> {

            Toast.makeText(getApplicationContext(), "Starting memory capture...", Toast.LENGTH_SHORT).show();

            if(interval != 0) {
                final int delay = interval * 1000;

                handler.postDelayed(runnable[0] = new Runnable() {
                    public void run() {
                        getInfo();
                        handler.postDelayed(this, delay);
                    }
                }, delay);
            }
            else {
                getInfo();
            }
        });

        button2.setOnClickListener(v -> {
            Toast.makeText(getApplicationContext(), "Memory capture stopped!", Toast.LENGTH_SHORT).show();

            handler.removeCallbacks(runnable[0]);
        });

        button3.setOnClickListener(v -> {
            try {
                String sample = Objects.requireNonNull(inputInterval.getText()).toString();
                interval = Integer.parseInt(sample);
                Toast.makeText(getApplicationContext(), "Interval set to " + interval + " seconds.", Toast.LENGTH_SHORT).show();
                String intervalTxt = "Current Interval: " + interval + " seconds";
                intervalText.setText(intervalTxt);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        button4.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Clear Captures")
                .setMessage("Are you sure you want to clear previous captures?")
                .setIcon(android.R.drawable.ic_menu_delete)
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                    File dir = getApplicationContext().getExternalFilesDir(null);
                    File file1 = new File(dir, "meminfo.txt");
                    File file2 = new File(dir, "pss.txt");

                    if(file1.delete() && file2.delete()){
                        Toast.makeText(MainActivity.this, "Captures cleared!", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(MainActivity.this, "No captures found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(android.R.string.no, null).show());

    }

    private void getMemoryInfo() {

        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();

        activityManager.getMemoryInfo(mi);

        Runtime runtime = Runtime.getRuntime();

        long systemTotal = mi.totalMem;
        long systemFree = mi.availMem;
        long systemUsed = systemTotal - systemFree;

        long nativeTotal = Debug.getNativeHeapSize();
        long nativeFree = Debug.getNativeHeapFreeSize();
        long nativeUsed = nativeTotal - nativeFree;

        long runtimeMax = runtime.maxMemory();
        long runtimeTotal = runtime.totalMemory();
        long runtimeFree = runtime.freeMemory();
        long runtimeUsed = runtimeTotal - runtimeFree;


        String content = "Total Memory : " + systemTotal + " kB\n" +
                "Free Memory : " + systemFree + " kB\n" +
                "Used Memory : " + systemUsed + " kB\n" +
                "Runtime Max Memory : " + runtimeMax + " kB\n" +
                "Runtime Total Memory : " + runtimeTotal + " kB\n" +
                "Runtime Free Memory : " + runtimeFree + " kB\n" +
                "Runtime Used Memory : " + runtimeUsed + " kB\n" +
                "Native Total Memory : " + nativeTotal + " kB\n" +
                "Native Free Memory : " + nativeFree + " kB\n" +
                "Native Used Memory : " + nativeUsed + " kB\n";

        writeToFile(getApplicationContext(), "meminfo.txt", content);

    }

    private void getProcessInfo() {

        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();

        Map<Integer, String> pidMap = new TreeMap<>();
        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : runningAppProcesses)
        {
            pidMap.put(runningAppProcessInfo.pid, runningAppProcessInfo.processName);
        }
        Collection<Integer> keys = pidMap.keySet();
        StringBuilder builder = new StringBuilder();

        for(int key : keys)
        {
            int[] pids = new int[1];
            pids[0] = key;
            android.os.Debug.MemoryInfo[] memoryInfoArray = activityManager.getProcessMemoryInfo(pids);
            for(android.os.Debug.MemoryInfo pidMemoryInfo: memoryInfoArray)
            {
                builder.append("Process ID : ").append(pids[0]).append("\n").
                        append("Process : ").append(pidMap.get(pids[0])).append("\n").
                        append("Total Private Dirty : ").append(pidMemoryInfo.getTotalPrivateDirty()).append(" kB\n").
                        append("Total PSS : ").append(pidMemoryInfo.getTotalPss()).append(" kB\n").
                        append("Total Shared Dirty : ").append(pidMemoryInfo.getTotalSharedDirty()).append(" kB\n").
                        append("Dalvik Dirty : ").append(pidMemoryInfo.dalvikPrivateDirty).append(" kB\n").
                        append("Dalvik PSS : ").append(pidMemoryInfo.dalvikPss).append(" kB\n").
                        append("Dalvik Dirty : ").append(pidMemoryInfo.dalvikSharedDirty).append(" kB\n").
                        append("Native Dirty : ").append(pidMemoryInfo.nativePrivateDirty).append(" kB\n").
                        append("Native PSS : ").append(pidMemoryInfo.nativePss).append(" kB\n").
                        append("Native Dirty : ").append(pidMemoryInfo.nativeSharedDirty).append(" kB\n\n");
            }
        }
        writeToFile(getApplicationContext(), "pss.txt", builder.toString());

    }

    private void writeToFile(Context context, String filename, String content) {
        File path = context.getExternalFilesDir(null);

        File output = new File(path + File.separator + filename);

        try {
            FileOutputStream fos = new FileOutputStream(output.getAbsolutePath(), true);
            OutputStreamWriter outputWriter=new OutputStreamWriter(fos);
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            outputWriter.append("\n\nTimestamp :").append(String.valueOf(timestamp)).append("\n").append(content);
            outputWriter.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getInfo() {
        getMemoryInfo();
        getProcessInfo();
        Toast.makeText(getApplicationContext(),"Fetching memory info...",Toast.LENGTH_SHORT).show();
    }

}