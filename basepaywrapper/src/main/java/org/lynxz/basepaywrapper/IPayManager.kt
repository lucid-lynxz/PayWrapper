package org.lynxz.basepaywrapper

import android.app.Application
import org.lynxz.basepaywrapper.observer.IOnPayResult

interface IPayManager {
    /**
     * 初始化操作
     * 微信需要传入 appId 和 appSecret 信息
     * 支付宝不需要,可放空
     * */
    fun init(application: Application, appId: String? = null, appSecret: String? = null)

    /**
     * 反初始化操作
     * 用于释放资源,结束子线程等
     * */
    fun uninit()

    /**
     * 是否已初始化
     * */
    fun isInitialized(): Boolean

    /**
     * 发起支付
     * 微信: 内部根据 orderJsonByServer 提取所需的订单参数,拼接成 PayReq 对象后调起微信支付功能
     * 支付宝: 内部根据 orderJsonByServer 生成 Map<String,String>
     *          PayTask alipay = new PayTask(YourPayActivity.this);
     *          Map<String, String> result = alipay.payV2(orderJsonByServer, true);
     * @param orderJsonByServer app对应的商家后台返回的订单信息
     * @param onPayResult 支付完成后回调通知用户
     * */
    fun pay(orderJsonByServer: String, onPayResult: IOnPayResult? = null): Boolean
}