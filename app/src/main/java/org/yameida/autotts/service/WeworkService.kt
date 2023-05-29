package org.yameida.autotts.service

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.FileObserver
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.blankj.utilcode.util.*
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.yameida.autotts.Constant
import org.yameida.autotts.observer.MultiFileObserver
import org.yameida.autotts.utils.*
import java.lang.Exception

/**
 * 辅助服务
 * rootInActiveWindow获取的是当前交互界面窗口的根view 需要验证包名
 * event.source则不需要验证包名获取窗口并可获得事件详情
 */
class WeworkService : AccessibilityService() {
    private val TAG = "WeworkService"
    lateinit var webSocketManager: WebSocketManager
    var currentPackage = ""
    var currentClass = ""
    var currentClassPackage = ""

    companion object {
        private var mFileObserver: FileObserver? = null
    }

    override fun onServiceConnected() {
        LogUtils.i("初始化成功")
        WeworkController.weworkService = this
        //初始化长连接
        initWebSocket()
        //初始化消息处理器
        MyLooper.init()
        //初始化图片接收
        initObserver()

        //监听是否修改链接号并重新长连接
        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.getStringExtra("type") == "modify_channel") {
                    LogUtils.e("更新channel")
                    webSocketManager.close(1000, "modify_channel")
                    initWebSocket()
                }
            }
        }, IntentFilter(Constant.WEWORK_NOTIFY))
    }

    private fun initWebSocket() {
        val url = Constant.getWsUrl()
        val listener = EchoWebSocketListener()
        LogUtils.d("initWebSocket: $url")
        webSocketManager = WebSocketManager(url, listener)
    }

    private fun initObserver() {
        if (!Constant.pushImage) return
        try {
            LogUtils.d("initObserver... mFileObserver is null ? ${mFileObserver == null}")
            if (mFileObserver == null) {
                mFileObserver =
                    MultiFileObserver("/storage/emulated/0/Android/data/com.tencent.wework/files/imagecache/imagemsg2");
                mFileObserver?.startWatching()
                log("startWatching...")
            } else {
                mFileObserver?.stopWatching()
                mFileObserver?.startWatching()
                log("restartWatching...")
            }
        } catch (e: Exception) {
            LogUtils.e(e)
            error("initObserver startWatching error!: ${e.message}")
        }
    }

    /**
     * TYPE_WINDOW_CONTENT_CHANGED 内容变化
     * TYPE_VIEW_SCROLLED 列表滚动
     * @param event
     */
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        currentPackage = event.packageName?.toString() ?: ""
        val className = event.className?.toString() ?: ""
        if (className.contains(currentPackage)) {
            LogUtils.d("更新当前页面: currentPackage: ${event.packageName} className: ${event.className}")
            currentClass = className
            currentClassPackage = currentPackage
        }
    }

    override fun onInterrupt() {
        LogUtils.i("onInterrupt")
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtils.i("onDestroy")
        //关闭自动回复
        WeworkController.enableLoopRunning = false
        webSocketManager.close(1000, "service Destroy")
    }

    inner class EchoWebSocketListener : WebSocketListener() {
        private val TAG = "WeworkService.EchoWebSocketListener"
        private lateinit var socket: WebSocket
        override fun onOpen(webSocket: WebSocket, response: Response) {
            socket = webSocket
            Log.e(TAG, "连接建立")
            val robotId = Constant.robotId
            val appVersion = SPUtils.getInstance().getString("appVersion", "")
            val workVersion = SPUtils.getInstance().getString("workVersion", "")
            val deviceRooted = SPUtils.getInstance().getBoolean("deviceRooted", false)
            val hook = SPUtils.getInstance().getBoolean("hook", false)
            log("连接建立: $robotId appVersion: $appVersion workVersion: $workVersion deviceRooted: $deviceRooted hook: $hook")
            LogUtils.i("设置自动跳转")
            sendBroadcast(true)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            LogUtils.i("onMessage: ${ if (text.length > 1000) (text.substring(0, 1000) + "...") else text }")
            try {
                MyLooper.onMessage(webSocket, text)
            } catch (e: Exception) {
                LogUtils.e(e)
                error(e.message)
            }
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosed(webSocket, code, reason)
            //服务器关闭后
            Log.e(TAG, "连接关闭 $reason")
            sendBroadcast(false)
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosing(webSocket, code, reason)
            socket.close(code, reason)
            Log.e(TAG, "服务端关闭连接 $code: $reason")
            sendBroadcast(false)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            //服务器中断
            Log.e(TAG, "连接错误: " + t.toString() + response.toString())
            sendBroadcast(false)
        }

        private fun sendBroadcast(switch: Boolean) {
            sendBroadcast(Intent(Constant.WEWORK_NOTIFY).apply {
                putExtra("type", "openWs")
                putExtra("switch", switch)
            })
        }
    }
}