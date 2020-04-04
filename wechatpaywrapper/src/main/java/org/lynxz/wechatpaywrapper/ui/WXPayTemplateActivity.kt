package org.lynxz.wechatpaywrapper.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import org.lynxz.wechatpaywrapper.util.LoggerUtil
import org.lynxz.wechatpaywrapper.util.WechatUtil

/**
 * 微信字符回调页面模板页
 * */
open class WXPayTemplateActivity : Activity(), IWXAPIEventHandler {
    companion object {
        const val TAG = "WXPayTemplateActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WechatUtil.getInstance().wxApi.handleIntent(intent, this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        WechatUtil.getInstance().wxApi.handleIntent(intent, this)
    }

    override fun onResp(baseResp: BaseResp?) {
        // errCode 0-成功 -1-错误(签名错误、未注册APPID等) -2-用户取消
        LoggerUtil.w(
            TAG,
            "onResp: type=${baseResp?.type}, errCode=${baseResp?.errCode},errStr=${baseResp?.errStr}"
        )


//        when (baseResp?.type) {
//            ConstantsAPI.COMMAND_PAY_BY_WX -> {
//            }
//        }
    }

    override fun onReq(baseReq: BaseReq?) {
        LoggerUtil.w(
            TAG,
            "onReq: type=${baseReq?.type}, transaction=${baseReq?.transaction}"
        )
    }
}