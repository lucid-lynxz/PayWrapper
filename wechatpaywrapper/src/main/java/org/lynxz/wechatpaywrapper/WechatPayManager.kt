package org.lynxz.wechatpaywrapper

import android.app.Application
import com.tencent.mm.opensdk.modelpay.PayReq
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
     * P.S. 内部根据 orderJsonByServer 提取所需的订单参数,拼接成 PayReq 对象后调起微信支付功能
     * @param orderJsonByServer app对应的商家后台返回的订单信息
     * */
    fun pay(orderJsonByServer: String) = WechatUtil.getInstance().pay(orderJsonByServer)

    /**
     * 发起微信字符
     * */
    fun pay(payReq: PayReq?) = WechatUtil.getInstance().pay(payReq)
}