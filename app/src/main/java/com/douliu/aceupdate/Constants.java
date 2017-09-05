package com.douliu.aceupdate;

import android.os.Environment;

import java.io.File;

/**
 *
 *
 * Created by douliu on 2017/9/5.
 *
 */

public class Constants {

    private static final String SD_CARD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();

    public static final String PATCH_PATH = SD_CARD_PATH + File.separatorChar + "apk.patch";

    public static final String NEW_PATH = SD_CARD_PATH + File.separatorChar + "new.apk";


}
