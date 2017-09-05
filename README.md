
# 增量更新解决方案

#### 如何更新app?
1. 从服务器端重新下载全新的安装包,覆盖安装
2. 服务器通过新的apk和旧的apk生成差分包,而客户端下载差分包,在本地对patch和旧的安装包进行合并,从而生成新的安装包,覆盖安装,实现增量更新

#### 增量更新所用到开源库

[Binary diff/patch utility](http://www.daemonology.net/bsdiff/)

diff/patch依赖了[bzip2](http://www.bzip.org/)

#### 服务器端

- 编写java本地方法,生成.h头文件

```
package com.example;

public class DiffUtils {
	
	
	public static native void diff(String oldPath,String newPath,String patchPath);
	
	static{
		System.loadLibrary("libDiff");
	}
	
	public static void main(String[] args) {
		
		String oldPath = "F:\\Path_Test\\old_58.apk";
		String newPath = "F:\\Path_Test\\new_585.apk";
		String patchPath = "F:\\Path_Test\\apk.patch";
		
		diff(oldPath, newPath, patchPath);
	}

}

```
- 拷贝`bzip2`和`bsdiff`源文件到C工程中,编写JNI代码,在`bsdiff.cpp`文件中将`main`函数改成`bsdiff_main`,引入生成的头文件,实现jni方法,如下


```
/*
 * Class:     com_example_DiffUtils
 * Method:    diff
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_example_DiffUtils_diff
(JNIEnv *env, jclass jcls, jstring old_path, jstring new_path, jstring patch_path){

	printf("jni diff start...");

	char* oldPath = const_cast<char *>(env->GetStringUTFChars(old_path, NULL));
	char* newPath = const_cast<char *>(env->GetStringUTFChars(new_path, NULL));
	char* patchPath = const_cast<char *>(env->GetStringUTFChars(patch_path, NULL));

	int argv = 4;
	char* args[4];

	args[0] = const_cast<char *>("bisdiff");
	args[1] = oldPath;
	args[2] = newPath;
	args[3] = patchPath;

	bsdiff_main(argv, args);


	env->ReleaseStringUTFChars(old_path, oldPath);
	env->ReleaseStringUTFChars(new_path, newPath);
	env->ReleaseStringUTFChars(patch_path, patchPath);


	printf("jni diff end...");

}
```
- 编译成.dll动态库,供服务器使用

#### 客户端 

1. 拷贝`bspatch.c`和`bzip2`源文件到`cpp`目录,配置
`CMakeLists.txt`

```

file(GLOB my_c_path src/main/cpp/bzip2/*.c)
add_library( # Sets the name of the library.
             native-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             ${my_c_path}
             src/main/cpp/bspatch.c )
```

2. 编写`java`本地方法

```

public class BsPatch {

    public static native int patch(String oldPath,String patchPath,String newPath);

    static {
        System.loadLibrary("native-lib");
    }

}
```
3. 将`bspatch.c`中的`main`改成`bspatch_main`,实现`jni`方法,如下

```
JNIEXPORT jint JNICALL
Java_com_douliu_aceupdate_BsPatch_patch(JNIEnv *env, jclass type, jstring oldPath_,
                                        jstring newPath_, jstring patchPath_) {

    LOGD("JNI BSPATCH START");

    const char *oldPath = (*env)->GetStringUTFChars(env, oldPath_, 0);
    const char *patchPath = (*env)->GetStringUTFChars(env, patchPath_, 0);
    const char *newPath = (*env)->GetStringUTFChars(env, newPath_, 0);

    int argv = 4;
    char* args[4];
    int ret;

    args[0] = "bspatch";
    args[1] = (char *) oldPath;
    args[2] = (char *) newPath;
    args[3] = (char *) patchPath;

    ret = patch_main(argv,args);
    if (ret != 0){
        LOGD("合并失败");
        return ret;
    }



    (*env)->ReleaseStringUTFChars(env, oldPath_, oldPath);
    (*env)->ReleaseStringUTFChars(env, patchPath_, patchPath);
    (*env)->ReleaseStringUTFChars(env, newPath_, newPath);

    LOGD("JNI BSPATCH END");

    return 0;

}
```

4. 下载服务器差分包
5. 获取本地旧的安装包

```
   /**
     * 获取已安装Apk文件的源Apk文件
     * 如：/data/app/my.apk
     *
     * @param context
     * @param packageName
     * @return
     */
    public static String getSourceApkPath(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName))
            return null;

        try {
            ApplicationInfo appInfo = context.getPackageManager()
                    .getApplicationInfo(packageName, 0);
            return appInfo.sourceDir;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
```
6. 进行合并并安装
