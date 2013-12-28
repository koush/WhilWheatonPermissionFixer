package com.koushikdutta.wesleycrusher;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class MyActivity extends Activity {
    void engage() {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle("Let me show you my science project");
        dialog.setIndeterminate(false);
        dialog.setProgress(0);
        dialog.setSecondaryProgress(0);
//        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setButton(ProgressDialog.BUTTON_NEGATIVE, getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        dialog.show();
        new Thread() {
            @Override
            public void run() {
                super.run();
                final List<PackageInfo> pkgs = getPackageManager().getInstalledPackages(0);
                int count = 0;
                for (final PackageInfo pi: pkgs) {
                    if (isFinishing())
                        return;

                    count++;
                    int uid = pi.applicationInfo.uid;
                    String dataDir = pi.applicationInfo.dataDir;
                    String command = String.format("busybox chown -R %s:%s %s", uid, uid, dataDir);
                    command += String.format(" ; busybox chmod -R u+w,u+r %s", dataDir);
                    final int fcount = count;
                    try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                int percent = 100 * fcount / pkgs.size();
                                dialog.setMessage(pi.applicationInfo.loadLabel(getPackageManager()));
                                dialog.setProgress(percent);
                                dialog.setSecondaryProgress(percent);
                            }
                        });
                        Process p = Runtime.getRuntime().exec(new String[] { "/system/xbin/su", "-c", command });
                        p.waitFor();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        new AlertDialog.Builder(MyActivity.this)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setCancelable(false)
                        .setTitle("Mission Successful")
                        .setMessage("Don't let the airlock door hit you on the way out.")
                        .create()
                        .show();
                    }
                });
            }
        }.start();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        findViewById(R.id.engage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                engage();
            }
        });
    }
}
