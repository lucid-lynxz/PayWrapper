package org.lynxz.paywrapper

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.lynxz.alipaywrapper.AliPayManager
import org.lynxz.basepaywrapper.PayManager
import org.lynxz.basepaywrapper.bean.PayType
import org.lynxz.basepaywrapper.observer.IOnPayResult
import org.lynxz.basepaywrapper.util.LoggerUtil
import org.lynxz.paywrapper.util.OrderInfoUtil2_0
import org.lynxz.third_generation.annotation.ThirdActivityAutoGenerator
import org.lynxz.wechatpaywrapper.WechatPayManager
import org.lynxz.wechatpaywrapper.ui.WXPayTemplateActivity

@ThirdActivityAutoGenerator(
    getApplicationId = BuildConfig.APPLICATION_ID,
    getSubPackageName = "wxapi",
    getTargetActivityName = "WXPayEntryActivity",
    getSupperClass = WXPayTemplateActivity::class
)
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_init.setOnClickListener {
            WechatPayManager.init(
                application,
                BuildConfig.WECHAT_APP_ID,
                BuildConfig.WECHAT_APP_SECRET
            )

            AliPayManager.init(application, null, null)

            PayManager.registerPayType(PayType.WechatPay, WechatPayManager)
                .registerPayType(PayType.AliPay, AliPayManager)

        }

        btn_pay_by_wx.setOnClickListener {
            // 根据微信官方demo代码 (https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=11_1)
            // 可以通过访问该网址生成后台临时测试订单信息: https://wxpay.wxutil.com/pub_v2/app/app_pay.php
            val orderInfo =
                "{\"appid\":\"wxb4ba3c02aa476ea1\",\"partnerid\":\"1900006771\",\"package\":\"Sign=WXPay\",\"noncestr\":\"2df79046fd66f15757297d443c88b975\",\"timestamp\":1586018924,\"prepayid\":\"wx05004844040057e5442cc5261140565432\",\"sign\":\"66C8E6811E63895B139D1DB97EF6335D\"}"
            PayManager.pay(PayType.WechatPay, orderInfo, onPayResult)
        }

        btn_pay_by_alipay.setOnClickListener { testAliPay() }
    }

    /**
     * 测试支付宝付款
     *
     * 这里只是为了方便直接向商户展示支付宝的整个支付流程；所以Demo中加签过程直接放在客户端完成；
     * * 真实App里，privateKey等数据严禁放在客户端，加签过程务必要放在服务端完成；
     * 防止商户私密数据泄露，造成不必要的资金损失，及面临各种安全风险；
     *
     * orderInfo 的获取必须来自服务端；
     *
     * 可使用支付宝提供的工具生成密钥: https://opendocs.alipay.com/open/291/106097
     */
    private fun testAliPay() {
        if (BuildConfig.ALIPAY_APPID.isBlank() || BuildConfig.ALIPAY_RSA2_PRIVATE.isBlank()) {
            tv_info.text = "请填入你的支付宝应用 appId 和 rsaV2 密钥"
            return
        }

        val params: Map<String, String> =
            OrderInfoUtil2_0.buildOrderParamMap(BuildConfig.ALIPAY_APPID, true)
        val orderParam: String = OrderInfoUtil2_0.buildOrderParam(params)

        val sign: String = OrderInfoUtil2_0.getSign(params, BuildConfig.ALIPAY_RSA2_PRIVATE, true)
        val orderInfo = "$orderParam&$sign"

        LoggerUtil.w("alipayOrderInfo", orderInfo)
        PayManager.pay(PayType.AliPay, orderInfo, onPayResult)
    }

    private val onPayResult = object : IOnPayResult {
        override fun onPayFinish(
            payType: PayType,
            success: Boolean,
            statusCode: String?,
            errMsg: String?,
            oriPayResultObj: Any?
        ) {
            tv_info.post {
                tv_info.text =
                    "${payType.name} 支付结果success:$success code:$statusCode errMsg:$errMsg\noriPayResultObj:$oriPayResultObj"
            }
        }
    }
}
