package com.douliu.aceupdate;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ApkUtils.getVersionCode(this) < 2.0) {
            new PatchTask().execute();
        } else {
            Toast.makeText(this, "需要要更新", Toast.LENGTH_SHORT).show();
        }

    }


    private class PatchTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            String oldPath = ApkUtils.getSourceApkPath(MainActivity.this, getPackageName());
            int success = BsPatch.patch(oldPath, Constants.PATCH_PATH, Constants.NEW_PATH);
            return success == 0;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                ApkUtils.install(MainActivity.this, Constants.NEW_PATH);
            } else {
                Toast.makeText(MainActivity.this,"合并失败",Toast.LENGTH_SHORT).show();
            }
        }
    }

}

