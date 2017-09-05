package com.douliu.aceupdate;

/**
 *
 *
 * Created by douliu on 2017/9/5.
 */

public class BsPatch {

    public static native int patch(String oldPath,String patchPath,String newPath);

    static {
        System.loadLibrary("native-lib");
    }

}
