package com.example.memorytracker;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Debug;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.io.*;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView txt = (TextView) findViewById(R.id.txt);
        txt.setText(getMemoryInfo());

        final TextView txt2 = (TextView) findViewById(R.id.process);
        txt2.setText(getProcessInfo());


        final TextView txt4 = (TextView) findViewById(R.id.home);

        final Button button1 = findViewById(R.id.tot_btn);
        final Button button2 = findViewById(R.id.pro_btn);

        final Button button4 = findViewById(R.id.re_btn);

        button1.setOnClickListener(v -> {
            txt.setVisibility(View.VISIBLE);
            txt2.setVisibility(View.GONE);

            txt4.setVisibility(View.GONE);
        });

        button2.setOnClickListener(v -> {
            txt.setVisibility(View.GONE);
            txt2.setVisibility(View.VISIBLE);

            txt4.setVisibility(View.GONE);
        });


        button4.setOnClickListener(v -> {
            // finish();
            // overridePendingTransition(0, 0);
            // startActivity(getIntent());
            // overridePendingTransition(0, 0);

            getMemoryInfo();
            getProcessInfo();

        });

    }

    private String getMemoryInfo() {

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

        String heading = "ActivityManager.MemoryInfo" + "\n" + "____________" + "\n\n";

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

        return heading + content;
    }

    private String getProcessInfo() {

        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();

        Map<Integer, String> pidMap = new TreeMap<>();
        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : runningAppProcesses)
        {
            pidMap.put(runningAppProcessInfo.pid, runningAppProcessInfo.processName);
        }
        Collection<Integer> keys = pidMap.keySet();
        StringBuilder builder = new StringBuilder();
        String heading = "ActivityManager.RunningAppProcessInfo" + "\n" + "____________" + "\n\n";
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
        return  heading + builder;
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

}