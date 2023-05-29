package org.yameida.autotts.service

/**
 * 客服端反转
 * 被服务端远程调用的服务Controller类
 */
object WeworkController {

    lateinit var weworkService: WeworkService
    var enableLoopRunning = false
    var mainLoopRunning = false


}