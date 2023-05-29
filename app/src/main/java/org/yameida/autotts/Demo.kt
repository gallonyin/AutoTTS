package org.yameida.autotts

import com.blankj.utilcode.util.LogUtils
import org.yameida.autotts.service.MyLooper
import org.yameida.autotts.service.WeworkOperationImpl
import org.yameida.autotts.service.sleep


/**
 * 示例
 */
object Demo {

    fun test() {
        LogUtils.d("test()")
        MyLooper.getInstance().removeCallbacksAndMessages(null)

        //打印当前视图树
//        AccessibilityUtil.printNodeClazzTree(getRoot())

        WeworkOperationImpl.getLink()


        sleep(5000)
    }



}