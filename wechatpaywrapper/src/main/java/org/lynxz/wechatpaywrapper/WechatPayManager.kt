package org.lynxz.wechatpaywrapper

import android.app.Application
import org.lynxz.wechatpaywrapper.util.WechatUtil


/**
 * 微信相关操作包装
 * 1. 先调用 [init] 进行出初始化操作
 * 2. 初始化后,通过 [pay] 调起微信字符
 * */
object WechatPayManager {
    lateinit var wechatAppId: String
    lateinit var wechatAppSecret: String
    lateinit var application: Application

    /**
     * 初始化操作, 传入微信的 appId 和 appSecret 信息
     * */
    fun init(app: Application, appId: String, appSecret: String) {
        application = app
        wechatAppId = appId
        wechatAppSecret = appSecret

        WechatUtil.getInstance().init(app)
    }

    /**
     * 发起微信支付
     * @param orderJsonByServer app对应的商家后台返回的订单信息
     * */
    fun pay(orderJsonByServer: String) = WechatUtil.getInstance().pay(orderJsonByServer)
}