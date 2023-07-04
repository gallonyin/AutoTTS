package org.yameida.autotts.service

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import com.blankj.utilcode.util.ClipboardUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.Utils
import com.hjq.toast.ToastUtils
import org.yameida.autotts.Constant
import org.yameida.autotts.activity.ListenActivity
import org.yameida.autotts.utils.AccessibilityExtraUtil
import org.yameida.autotts.utils.AccessibilityUtil
import org.yameida.autotts.utils.Views


/**
 * 全局操作类型 200 实现类
 */
object WeworkOperationImpl {

    /**
     * 获取文章链接
     */
    fun getLink(): Boolean {
        val currentApp = WeworkController.weworkService.currentClassPackage
        LogUtils.d("getLink(): $currentApp")
        val result = when (currentApp) {
            Constant.PACKAGE_NAMES_WECHAT -> {
                getWechatLink()
            }
            Constant.PACKAGE_NAMES_WEWORK -> {
                getWeworkLink()
            }
            else -> return false
        }
        if (Constant.autoBack == 1) {
            Utils.getApp().packageManager.getLaunchIntentForPackage(currentApp)?.apply {
                    this.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    Utils.getApp().startActivity(this)
                }
        }
        return result
    }

    /**
     * 获取微信文章链接
     */
    fun getWechatLink(): Boolean {
        LogUtils.d("getWechatLink()")
        if (AccessibilityExtraUtil.loadingPage("TmplWebViewMMUI", "MMWebViewUI")) {
            if (AccessibilityUtil.findDescAndClick(getRoot(), "更多信息", exact = true)) {
                if (AccessibilityUtil.findTextAndClick(getRoot(), "复制链接", exact = true)) {
                    return execBrowser()
                } else {
                    LogUtils.e("未找到复制链接按钮")
                    return false
                }
            } else {
                LogUtils.e("未找到更多信息按钮")
                return false
            }
        } else {
            LogUtils.e("当前不在文章页面")
            return false
        }
    }

    /**
     * 获取企微文章链接
     */
    fun getWeworkLink(): Boolean {
        LogUtils.d("getWeworkLink()")
        if (AccessibilityExtraUtil.loadingPage("NewsFeedWebActivity")) {
            val tvList = AccessibilityUtil.findAllByClazz(getRoot(), Views.TextView)
            var flag = -1
            tvList.forEachIndexed { index, node ->
                if (node.text?.toString() == "行业资讯") {
                    flag = index + 1
                }
            }
            if (flag > 0 && flag < tvList.size) {
                AccessibilityUtil.performClick(tvList[flag])
                if (AccessibilityUtil.findTextAndClick(getRoot(), "复制链接", exact = true)) {
                    return execBrowser()
                } else {
                    LogUtils.e("未找到复制链接按钮")
                    return false
                }
            } else {
                LogUtils.e("未找到行业资讯标题")
                return false
            }
        } else {
            LogUtils.e("当前不在文章页面")
            return false
        }
    }

    /**
     * 执行浏览器
     */
    fun execBrowser(): Boolean {
        LogUtils.d("execBrowser()")
        ListenActivity.enterActivity(Utils.getApp(), 0)
        var clipboardText = ""
        if (AccessibilityExtraUtil.loadingPage("ListenActivity")) {
            clipboardText = ClipboardUtils.getText().toString()
            LogUtils.d("剪切板内容: $clipboardText")
            if (clipboardText.isEmpty()) {
                sleep(Constant.CHANGE_PAGE_INTERVAL)
                clipboardText = ClipboardUtils.getText().toString()
                LogUtils.d("再次获取剪切板内容: $clipboardText")
            }
        }
        if (!clipboardText.startsWith("http")) {
            LogUtils.e("复制链接错误: $clipboardText")
            return false
        }
        try {
            val intent = Intent()
            intent.action = "android.intent.action.VIEW"
            intent.data = Uri.parse(clipboardText)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            when (Constant.browserType) {
                Constant.BROWSER_TYPE_BAIDU -> {
                    intent.setClassName("com.baidu.searchbox","com.baidu.searchbox.MainActivity")
                }
                Constant.BROWSER_TYPE_QQ -> {
                    intent.setClassName("com.tencent.mtt","com.tencent.mtt.MainActivity")
                }
                Constant.BROWSER_TYPE_UC -> {
                    intent.setClassName("com.UCMobile","com.uc.browser.InnerUCMobile")
                }
                Constant.BROWSER_TYPE_KK -> {
                    com.blankj.utilcode.util.ToastUtils.showLong("设置为夸克浏览器")
                }
                Constant.BROWSER_TYPE_SG -> {
                    com.blankj.utilcode.util.ToastUtils.showLong("设置为搜狗浏览器")
                }
            }
            //系统浏览器
//            intent.setClassName("com.android.browser","com.android.browser.BrowserActivity")
            Utils.getApp().startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            LogUtils.e("未安装指定浏览器，请安装~")
            ToastUtils.show("未安装指定浏览器，请安装~")
            e.printStackTrace()
            return false
        }
        when (Constant.browserType) {
            Constant.BROWSER_TYPE_BAIDU -> {
                return execBaiduBrowser()
            }
            Constant.BROWSER_TYPE_QQ -> {
                return execQQBrowser()
            }
            Constant.BROWSER_TYPE_UC -> {
                return execBaiduBrowser()
            }
            Constant.BROWSER_TYPE_KK -> {
                return execBaiduBrowser()
            }
            Constant.BROWSER_TYPE_SG -> {
                return execBaiduBrowser()
            }
        }
        LogUtils.e("未找到可执行的浏览器")
        return false
    }

    /**
     * 百度浏览器
     */
    private fun execBaiduBrowser(): Boolean {
        LogUtils.d("execBaiduBrowser()")
        AccessibilityExtraUtil.loadingPage("MainActivity", "HotSplashActivity")
        if (WeworkController.weworkService.currentClass == "com.baidu.searchbox.introduction.hotboot.HotSplashActivity") {
            AccessibilityUtil.findTextAndClick(getRoot(), "跳过")
            LogUtils.d("尝试跳过")
            if (!AccessibilityExtraUtil.loadingPage("MainActivity", "SlidingPaneLayout", timeout = Constant.CHANGE_PAGE_INTERVAL)) {
                AccessibilityUtil.clickByNode(WeworkController.weworkService, AccessibilityUtil.findOnceByText(getRoot(), "跳过"))
                LogUtils.d("再次尝试跳过")
                AccessibilityExtraUtil.loadingPage("MainActivity", "SlidingPaneLayout")
            }
        }
        if (AccessibilityUtil.findDescAndClick(getRoot(), "关闭", exact = true, timeout = Constant.LONG_INTERVAL * 2)) {
            LogUtils.d("打开更多功能")
            if (AccessibilityUtil.findTextAndClick(getRoot(), "语音播报", exact = true)) {
                LogUtils.d("打开语音播报")
                AccessibilityUtil.findOneByClazz(getRoot(), Views.ProgressBar)
                if (AccessibilityUtil.waitForClazzMissing(getRoot(), Views.ProgressBar, timeout = Constant.LONG_INTERVAL * 2)) {
                    LogUtils.d("进度条加载完成")
                    return true
                } else {
                    LogUtils.e("进度条加载异常")
                    return false
                }
            } else {
                LogUtils.e("未找到语音播报按钮")
                return false
            }
        } else {
            LogUtils.e("未找到更多功能按钮")
            return false
        }
    }

    /**
     * QQ浏览器
     */
    private fun execQQBrowser(): Boolean {
        LogUtils.d("execQQBrowser()")
        if (AccessibilityExtraUtil.waitForPageMissing("MainActivity", timeout = Constant.LONG_INTERVAL * 2)) {
            LogUtils.d("浏览器打开链接")
            if (AccessibilityUtil.findDescAndClick(getRoot(), "关闭", exact = true)) {
                LogUtils.d("打开更多功能")
                if (AccessibilityUtil.findTextAndClick(getRoot(), "语音播报", exact = true)) {
                    LogUtils.d("打开语音播报")
                    AccessibilityUtil.findOneByClazz(getRoot(), Views.ProgressBar)
                    if (AccessibilityUtil.waitForClazzMissing(getRoot(), Views.ProgressBar, timeout = Constant.LONG_INTERVAL * 2)) {
                        LogUtils.d("进度条加载完成")
                        return true
                    } else {
                        LogUtils.e("进度条加载异常")
                        return false
                    }
                } else {
                    LogUtils.e("未找到语音播报按钮")
                    return false
                }
            } else {
                LogUtils.e("未找到更多功能按钮")
                return false
            }
        } else {
            LogUtils.e("浏览器未能正常打开链接")
            return false
        }
    }

}