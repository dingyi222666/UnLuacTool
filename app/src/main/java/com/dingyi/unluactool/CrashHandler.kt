package com.dingyi.unluactool

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION
import android.os.Environment
import android.util.Log
import com.dingyi.unluactool.common.ktx.getJavaClass
import java.io.*
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*

/**
 * UncaughtException处理类,当程序发生Uncaught异常的时候,有该类来接管程序,并记录发送错误报告.
 *
 * @author user
 */
object CrashHandler
/** 保证只有一个CrashHandler实例  */
    : Thread.UncaughtExceptionHandler {
    //系统默认的UncaughtException处理类
    private var mDefaultHandler: Thread.UncaughtExceptionHandler? = null

    //程序的Context对象
    private var mContext = WeakReference<Context>(null)

    //用来存储设备信息和异常信息
    private val mDeviceInfoList = mutableMapOf<String, String>()

    //用于格式化日期,作为日志文件名的一部分
    private val formatter = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")

    /**
     * 初始化
     *
     * @param context
     */
    fun init(context: Context) {
        mContext = WeakReference<Context>(context)
        //获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        //设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    override fun uncaughtException(thread: Thread, ex: Throwable) {
        if (!handleException(ex) && mDefaultHandler != null) {
            //如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler?.uncaughtException(thread, ex)
        } else {
            /*try
			{
				Thread.sleep(3000);
			}
			catch (InterruptedException e)
			{
				Log.e(TAG, "error : ", e);
			}
			//退出程序
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(0);*/
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private fun handleException(ex: Throwable): Boolean {
        //使用Toast来显示异常信息
        /*new Thread() {
			@Override
			public void run()
			{
				Looper.prepare();
				Toast.makeText(mContext, "很抱歉,程序出现异常,即将退出.", Toast.LENGTH_LONG).show();
				Looper.loop();
			}
		}.start();*/
        //收集设备参数信息
        mContext.get()?.let {
            collectDeviceInfo(it)
        }
        //保存日志文件
        saveCrashInfoToFile(ex)
        return true
    }

    /**
     * 收集设备参数信息
     * @param ctx
     */
    private fun collectDeviceInfo(ctx: Context) {
        runCatching {
            val pm = ctx.packageManager
            val pi = pm.getPackageInfo(ctx.packageName, PackageManager.GET_ACTIVITIES)
            if (pi != null) {
                val versionName = if (pi.versionName == null) "null" else pi.versionName
                val versionCode = pi.versionCode.toString() + ""
                mDeviceInfoList["versionName"] = versionName
                mDeviceInfoList["versionCode"] = versionCode
            }
        }.onFailure {
            Log.e(TAG, "an error occured when collect package info", it)
        }


        val allFields = listOf(
            getJavaClass<Build>(),
            getJavaClass<VERSION>()
        ).flatMap { clazz ->
            clazz.declaredFields.toList()
        }

        allFields.forEach { field ->
            kotlin.runCatching {
                field.isAccessible = true
                val obj = field[null]
                if (obj is Array<*>) mDeviceInfoList[field.name] =
                    obj.contentToString() else mDeviceInfoList[field.name] = obj.toString()
                Log.d(TAG, field.name + " : " + field[null])
            }.onFailure { e ->
                Log.e(TAG, "an error occured when collect crash info", e)
            }
        }

    }

    /**
     * 保存错误信息到文件中
     *
     * @param ex
     * @return 返回文件名称,便于将文件传送到服务器
     */
    private fun saveCrashInfoToFile(ex: Throwable): String? {
        val sb = StringBuffer()
        for ((key, value) in mDeviceInfoList) {
            sb.append("$key=$value\n")
        }
        val writer = StringWriter()
        val printWriter = PrintWriter(writer)
        ex.printStackTrace(printWriter)
        var cause = ex.cause
        while (cause != null) {
            cause.printStackTrace(printWriter)
            cause = cause.cause
        }
        printWriter.close()
        val result = writer.toString()
        sb.append(result)
        kotlin.runCatching {
            val timestamp = System.currentTimeMillis()
            val time = formatter.format(Date())
            val fileName = "crash-$time-$timestamp.log"
            if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                val path = "/sdcard/android/data/com.dingyi.unluactool/files/crash/"
                val dir = File(path)
                if (!dir.exists()) dir.mkdirs()
                val fos = FileOutputStream(path + fileName)
                //				FileOutputStream fos = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
                fos.write(sb.toString().toByteArray())
                Log.e("crash", sb.toString())
                fos.close()
            }
            return fileName
        }.onFailure { e ->
            Log.e(TAG, "an error occured while writing file...", e)
        }
        return null
    }


    const val TAG = "CrashHandler"

    /** 获取CrashHandler实例 ,单例模式  */
    //CrashHandler实例


}