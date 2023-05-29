package org.yameida.autotts.service

import android.os.Handler
import android.os.Looper
import android.os.Message
import com.blankj.utilcode.util.EncryptUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SPUtils
import com.google.gson.reflect.TypeToken
import okhttp3.WebSocket
import org.yameida.autotts.Constant
import org.yameida.autotts.model.ExecCallbackBean
import org.yameida.autotts.model.WeworkMessageBean
import org.yameida.autotts.model.WeworkMessageListBean
import org.yameida.autotts.utils.FloatWindowHelper
import java.nio.charset.StandardCharsets
import java.util.LinkedHashSet
import kotlin.concurrent.thread

object MyLooper {

    private var threadHandler: Handler? = null

    val looper = thread {
        LogUtils.i("myLooper starting...")
        Looper.prepare()
        val myLooper = Looper.myLooper()
        if (myLooper != null) {
            threadHandler = object : Handler(myLooper) {
                override fun handleMessage(msg: Message) {
                    while (FloatWindowHelper.isPause) {
                        LogUtils.i("主功能暂停...")
                        sleep(Constant.CHANGE_PAGE_INTERVAL)
                    }
                    LogUtils.d("handle message: " + Thread.currentThread().name, msg)
                    try {
                        dealWithMessage(msg.obj as WeworkMessageBean)
                    } catch (e: Exception) {
                        LogUtils.e(e)
                        error("执行异常尝试重试 ${e.message}")
                        try {
                            goHome()
                            dealWithMessage(msg.obj as WeworkMessageBean)
                        } catch (e: Exception) {
                            LogUtils.e(e)
                            error("执行异常重试仍失败 ${e.message}")
                            uploadCommandResult(msg.obj as WeworkMessageBean, ExecCallbackBean.ERROR_ILLEGAL_OPERATION, e.message ?: "", 0L)
                        }
                    }
                }
            }
        } else {
            LogUtils.e("myLooper is null!")
        }
        Looper.loop()
    }

    fun init() {
        LogUtils.i("init myLooper...")
        SPUtils.getInstance("noTipMessage").clear()
        SPUtils.getInstance("lastSyncMessage").clear()
        SPUtils.getInstance("noSyncMessage").clear()
        SPUtils.getInstance("limit").clear()
        SPUtils.getInstance("groupInvite").clear()
        SPUtils.getInstance("lastImage").clear()
        SPUtils.getInstance("myInfo").clear()
    }

    fun getInstance(): Handler {
        while (true) {
            threadHandler?.let { return it }
            LogUtils.e("threadHandler is not ready...")
            sleep(Constant.POP_WINDOW_INTERVAL / 5)
        }
    }

    fun onMessage(webSocket: WebSocket?, text: String) {
        val messageList: WeworkMessageListBean<WeworkMessageBean> =
            GsonUtils.fromJson<WeworkMessageListBean<WeworkMessageBean>>(text, object : TypeToken<WeworkMessageListBean<ExecCallbackBean>>(){}.type)
        if (messageList.socketType == WeworkMessageListBean.SOCKET_TYPE_HEARTBEAT) {
            return
        }
        if (messageList.socketType == WeworkMessageListBean.SOCKET_TYPE_MESSAGE_CONFIRM) {
            return
        }
        if (messageList.socketType == WeworkMessageListBean.SOCKET_TYPE_MESSAGE_LIST) {
            val confirm = WeworkController.weworkService.webSocketManager.confirm(messageList.messageId)
            if (!confirm) return
            if (messageList.encryptType == 1) {
                val decryptHexStringAES = EncryptUtils.decryptHexStringAES(
                    messageList.encryptedList,
                    Constant.key,
                    Constant.transformation,
                    Constant.iv
                )
                messageList.list =
                    GsonUtils.fromJson(
                        String(decryptHexStringAES, StandardCharsets.UTF_8),
                        object : TypeToken<ArrayList<WeworkMessageBean>>() {}.type
                    )
            }
            //去重处理 丢弃之前的重复指令 丢弃之前的获取新消息指令
            for (message in LinkedHashSet(messageList.list)) {
                if (message.type == WeworkMessageBean.LOOP_RECEIVE_NEW_MESSAGE) {
                    WeworkController.enableLoopRunning = true
                } else {
                    WeworkController.mainLoopRunning = false
                    LogUtils.v("加入指令到执行队列", if (message.fileBase64.isNullOrEmpty()) GsonUtils.toJson(message) else message.type)
                    getInstance().removeMessages(message.type * message.hashCode() + (System.currentTimeMillis() / 10000).toInt())
                    getInstance().sendMessage(Message.obtain().apply {
                        what = message.type * message.hashCode() + (System.currentTimeMillis() / 10000).toInt()
                        obj = message.apply {
                            messageId = messageList.messageId
                            apiSend = messageList.apiSend
                        }
                    })
                }
                getInstance().removeMessages(WeworkMessageBean.LOOP_RECEIVE_NEW_MESSAGE)
                getInstance().sendMessage(Message.obtain().apply {
                    what = WeworkMessageBean.LOOP_RECEIVE_NEW_MESSAGE
                    obj = WeworkMessageBean().apply { type = WeworkMessageBean.LOOP_RECEIVE_NEW_MESSAGE }
                })
            }
        }
    }

    private fun dealWithMessage(message: WeworkMessageBean) {
        when (message.type) {
        }
    }
}