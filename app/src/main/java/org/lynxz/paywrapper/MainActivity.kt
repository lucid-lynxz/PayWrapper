package org.lynxz.paywrapper

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
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
        }

        btn_pay.setOnClickListener {
            // 根据微信官方demo代码 (https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=11_1)
            // 可以通过访问该网址生成后台临时测试订单信息: https://wxpay.wxutil.com/pub_v2/app/app_pay.php
            val orderInfo =
                "{\"appid\":\"wxb4ba3c02aa476ea1\",\"partnerid\":\"1900006771\",\"package\":\"Sign=WXPay\",\"noncestr\":\"2df79046fd66f15757297d443c88b975\",\"timestamp\":1586018924,\"prepayid\":\"wx05004844040057e5442cc5261140565432\",\"sign\":\"66C8E6811E63895B139D1DB97EF6335D\"}"
            WechatPayManager.pay(orderInfo)
        }
    }
}
