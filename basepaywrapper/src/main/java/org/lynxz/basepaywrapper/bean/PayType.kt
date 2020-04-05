package org.lynxz.basepaywrapper.bean

/**
 * 支持的支付类型
 * */
sealed class PayType(val name: String) {
    // 微信支付
    object WechatPay : PayType("wechatPay")

    // 支付宝支付
    object AliPay : PayType("aliPay")
}