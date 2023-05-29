package org.yameida.autotts

import android.app.Application
import android.content.Intent
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.Utils
import com.efs.sdk.base.core.util.PackageUtil
import com.google.gson.Gson
import com.hjq.toast.ToastUtils
import com.tendcloud.tenddata.TalkingDataSDK
import com.umeng.commonsdk.UMConfigure
import org.yameida.autotts.config.GlobalException
import org.yameida.autotts.notification.PlayNotifyManager
import org.yameida.autotts.utils.LogUtilsInit
import update.UpdateAppUtils

class MyApplication : Application() {

    companion object {

        /**
         * 回到AutoTTS首页 需要先授权显示悬浮窗
         */
        fun launchIntent() {
            LogUtils.e("进入AutoTTS APP~")
            val app = Utils.getApp()
            app.packageManager.getLaunchIntentForPackage(PackageUtil.getPackageName(app))?.apply {
                this.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                app.startActivity(this)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        //初始化工具类
        Utils.init(this)
        //初始化Log工具配置
        LogUtilsInit.init()
        GsonUtils.setGsonDelegate(Gson())
        //初始化 Toast 框架
        ToastUtils.init(this)
        //初始化友盟统计
        val key = "6284a3a3d024421570f97c3c"
        val channel = "main_channel"
        UMConfigure.preInit(this, key, channel)
        //判断是否同意隐私协议，uminit为1时为已经同意，直接初始化umsdk
        if (SPUtils.getInstance().getString("uminit", "1") == "1") {
            UMConfigure.init(this, key, channel, UMConfigure.DEVICE_TYPE_PHONE, "")
        }
        TalkingDataSDK.init(this, "80E9C84E39904DAFB28562910FF7C86C", "autotts_master", Constant.robotId)
        //初始化自动更新
        UpdateAppUtils.init(this)
        //初始化前台服务
        PlayNotifyManager.show()
        //设置全局异常捕获重启
        Thread.setDefaultUncaughtExceptionHandler(GlobalException.getInstance())
    }

}