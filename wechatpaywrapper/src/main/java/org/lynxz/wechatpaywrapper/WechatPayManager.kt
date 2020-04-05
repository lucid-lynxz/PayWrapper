package org.lynxz.wechatpaywrapper

import android.app.Application
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelpay.PayReq
import org.lynxz.basepaywrapper.IPayManager
import org.lynxz.basepaywrapper.bean.PayType
import org.lynxz.basepaywrapper.observer.IOnPayResult
import org.lynxz.wechatpaywrapper.util.WechatUtil


/**
 * 微信相关操作包装
 * 1. 先调用 [init] 进行出初始化操作
 * 2. 初始化后,通过 [pay] 调起微信字符
 * */
object WechatPayManager : IPayManager {
    lateinit var wechatAppId: String
    lateinit var wechatAppSecret: String
    lateinit var application: Application
    private var isInitComplete = false
    private var pendingOnPayResult: IOnPayResult? = null // 支付结果回调监听

    /**
     * 初始化操作, 传入微信的 appId 和 appSecret 信息
     * */
    override fun init(application: Application, appId: String?, appSecret: String?) {
        this.application = application
        this.wechatAppId = appId ?: ""
        this.wechatAppSecret = appSecret ?: ""

        WechatUtil.getInstance().init(application)
        isInitComplete = true
    }

    override fun isInitialized() = isInitComplete

    /**
     * 发起微信支付
     * P.S. 内部根据 orderJsonByServer 提取所需的订单参数,拼接成 PayReq 对象后调起微信支付功能
     * @param orderJsonByServer app对应的商家后台返回的订单信息
     * */
    override fun pay(orderJsonByServer: String, onPayResult: IOnPayResult?): Boolean {
        pendingOnPayResult = onPayResult
        return WechatUtil.getInstance().pay(orderJsonByServer)
    }

    /**
     * 发起微信支付
     * 要求传入 payReq 对象类型才生效
     * */
    fun pay(payReq: PayReq, onPayResult: IOnPayResult?): Boolean {
        pendingOnPayResult = onPayResult
        return WechatUtil.getInstance().pay(payReq)
    }

    fun setPayResp(baseResp: BaseResp?) {
        baseResp?.let {
            // 支付结果
            if (it.type == ConstantsAPI.COMMAND_PAY_BY_WX) {
                pendingOnPayResult?.onPayFinish(
                    PayType.WechatPay,
                    it.errCode == 0, // 0:成功 -1:错误(签名错误、未注册APPID等)  2:用户取消
                    "${it.errCode}",
                    it.errStr,
                    baseResp
                )
            }
        }
    }
}