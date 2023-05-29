package org.yameida.autotts

import com.blankj.utilcode.util.SPUtils

object Constant {

    val AVAILABLE_VERSION = arrayListOf("")
    const val PACKAGE_NAMES_WEWORK = "com.tencent.wework"
    const val PACKAGE_NAMES_WECHAT = "com.tencent.mm"
    const val WEWORK_NOTIFY = "wework_notify"
    const val LONG_INTERVAL = 5000L
    const val CHANGE_PAGE_INTERVAL = 1000L
    const val POP_WINDOW_INTERVAL = 500L
    private const val DEFAULT_HOST = "wss://autotts.asrtts.cn"

    var key = "9876543210abcdef".toByteArray()
    var iv = "0123456789abcdef".toByteArray()
    val transformation = "AES/CBC/PKCS7Padding"

    const val BROWSER_TYPE_BAIDU = 0
    const val BROWSER_TYPE_QQ = 1
    const val BROWSER_TYPE_UC = 2
    const val BROWSER_TYPE_KK = 3
    const val BROWSER_TYPE_SG = 4
    var browserType: Int
        get() = SPUtils.getInstance().getInt("browserType", BROWSER_TYPE_BAIDU)
        set(value) {
            SPUtils.getInstance().put("browserType", value)
        }

    var weworkAgentId: String
        get() = SPUtils.getInstance().getString("weworkAgentId", "")
        set(value) {
            SPUtils.getInstance().put("weworkAgentId", value)
        }
    var weworkSchema: String
        get() = SPUtils.getInstance().getString("weworkSchema", "")
        set(value) {
            SPUtils.getInstance().put("weworkSchema", value)
        }
    var weworkMP: String
        get() = SPUtils.getInstance().getString("weworkMP", "")
        set(value) {
            SPUtils.getInstance().put("weworkMP", value)
        }
    var encryptType: Int = SPUtils.getInstance().getInt("encryptType", 1)
    var autoReply: Int = SPUtils.getInstance().getInt("autoReply", 1)
    var groupStrict: Boolean
        get() = SPUtils.getInstance().getBoolean("groupStrict", false)
        set(value) = SPUtils.getInstance().put("groupStrict", value)
    var friendRemarkStrict: Boolean
        get() = SPUtils.getInstance().getBoolean("friendRemarkStrict", false)
        set(value) = SPUtils.getInstance().put("friendRemarkStrict", value)
    var pushImage = false
    var autoPublishComment: Boolean
        get() = SPUtils.getInstance().getBoolean("autoPublishComment", true)
        set(value) = SPUtils.getInstance().put("autoPublishComment", value)
    var groupQrCode: Boolean
        get() = SPUtils.getInstance().getBoolean("groupQrCode", false)
        set(value) = SPUtils.getInstance().put("groupQrCode", value)
    var enableMediaProject = false
    var enableSdkShare = false
    var fullGroupName: Boolean
        get() = SPUtils.getInstance().getBoolean("fullGroupName", true)
        set(value) = SPUtils.getInstance().put("fullGroupName", value)
    var customLink: Boolean
        get() = SPUtils.getInstance().getBoolean("customLink", false)
        set(value) = SPUtils.getInstance().put("customLink", value)
    var customMP: Boolean
        get() = SPUtils.getInstance().getBoolean("customMP", false)
        set(value) = SPUtils.getInstance().put("customMP", value)
    var robotId: String
        get() = SPUtils.getInstance().getString("robotId", SPUtils.getInstance().getString("LISTEN_CHANNEL_ID", ""))
        set(value) {
            SPUtils.getInstance().put("robotId", value)
        }
    //replyStrategy=replyAll+1   replyStrategy=0不回复 replyStrategy=1回复at replyStrategy=2回复所有
    var replyStrategy: Int
        get() = SPUtils.getInstance().getInt("replyStrategy", 1)
        set(value) {
            SPUtils.getInstance().put("replyStrategy", value)
        }
    var qaUrl: String
        get() = SPUtils.getInstance().getString("qaUrl", "")
        set(value) {
            SPUtils.getInstance().put("qaUrl", value)
        }
    var openCallback: Int
        get() = SPUtils.getInstance().getInt("openCallback", 0)
        set(value) {
            SPUtils.getInstance().put("openCallback", value)
        }
    var host: String
        get() = SPUtils.getInstance().getString("host", DEFAULT_HOST)
        set(value) {
            SPUtils.getInstance().put("host", value)
        }

    fun getWsUrl() = "$host/webserver/wework/$robotId"

    fun getCheckUpdateUrl() = "${getBaseUrl()}/appUpdate/checkUpdate"

    fun getMyConfig() = "${getBaseUrl()}/robot/robotInfo/get?robotId=$robotId"

    fun getRobotUpdateUrl() = "${getBaseUrl()}/robot/robotInfo/update?robotId=$robotId"

    fun getTestUrl() = "${getBaseUrl()}/test"

    fun getPushLocalFileUrl() = "${getBaseUrl()}/fileUpload/upload?robotId=$robotId"

    private fun getBaseUrl() = host.replace("wss", "https").replace("ws", "http")

}
