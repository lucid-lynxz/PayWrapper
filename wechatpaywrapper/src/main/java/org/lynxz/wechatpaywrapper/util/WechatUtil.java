package org.lynxz.wechatpaywrapper.util;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.lynxz.basepaywrapper.util.LoggerUtil;
import org.lynxz.wechatpaywrapper.WechatPayManager;

/**
 * 微信操作相关工具类
 * 文档: https://developers.weixin.qq.com/doc/oplatform/Mobile_App/Access_Guide/Android.html
 * 使用:
 * 1. [前置条件] 调用 {@link #init(Application)} 进行初始化
 * 2. 通过 {@link #pay(PayReq)} 或者 {@link #pay(String)} 调起微信支付,最终结果会通过 WXPayEntryActivity 回调
 */
public class WechatUtil {
    private static final String TAG = "WechatUtil";
    private IntentFilter wechatFilter = new IntentFilter(ConstantsAPI.ACTION_REFRESH_WXAPP);

    private static class WechatUtilHolder {
        private static WechatUtil instance = new WechatUtil();
    }

    private WechatUtil() {
    }

    public static WechatUtil getInstance() {
        return WechatUtilHolder.instance;
    }

    // IWXAPI 是第三方app和微信通信的openApi接口
    private IWXAPI api;
    private BroadcastReceiver wechatReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 将该app注册到微信
            api.registerApp(WechatPayManager.wechatAppId);
        }
    };

    // 通过WXAPIFactory工厂，获取IWXAPI的实例
    public void init(@NotNull Application application) {
        api = WXAPIFactory.createWXAPI(application, WechatPayManager.wechatAppId, true);

        // 将应用的appId注册到微信
        api.registerApp(WechatPayManager.wechatAppId);

        //建议动态监听微信启动广播进行注册到微信
        application.registerReceiver(wechatReceiver, wechatFilter);
    }

    public void uninit(Application application) {
        if (api != null) {
            api.unregisterApp();
            api = null;
        }

        if (application != null) {
            application.unregisterReceiver(wechatReceiver);
        }
    }

    /**
     * 微信登录文档:https://developers.weixin.qq.com/doc/oplatform/Mobile_App/WeChat_Login/Development_Guide.html
     */
    public boolean login() {
        if (api == null) {
            LoggerUtil.w(TAG, "尚未初始化IWXAPI的实例,登录失败");
            return false;
        }

        if (!api.isWXAppInstalled()) {
            LoggerUtil.w(TAG, "您还未安装微信客户端,调用微信登录失败");
            return false;
        }
        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo"; // 应用授权作用域，如获取用户个人信息则填写 snsapi_userinfo
        req.state = "wechat_sdk_demo_test"; // 用于保持请求和回调的状态，授权请求后原样带回给第三方。该参数可用于防止 csrf 攻击（跨站请求伪造攻击），建议第三方带上该参数，可设置为简单的随机数加 session 进行校验
        return api.sendReq(req); // 第三方向微信终端发送一个SendAuthReq消息结构
    }

    /**
     * 发起微信支付
     * 文档: https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=8_5
     *
     * @param orderJsonByServer app对应的商家后台返回的订单信息
     *                          各字段含义见文档: https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=9_12&index=2
     * @return 发起支付请求是否成功
     */
    public boolean pay(String orderJsonByServer) {
        if (api == null) {
            LoggerUtil.e(TAG, "支付失败: 请先初始化后再试");
            return false;
        } else if (TextUtils.isEmpty(orderJsonByServer)) {
            LoggerUtil.e(TAG, "支付失败: 订单数据信息为空");
            return false;
        }

        try {
            JSONObject json = new JSONObject(orderJsonByServer);
            PayReq req = new PayReq();
            req.appId = json.getString("appid");  // 微信开放平台审核通过的应用APPID , 如wx9999
            req.partnerId = json.getString("partnerid"); // 微信支付分配的商户号
            req.prepayId = json.getString("prepayid"); // 预支付交易会话ID
            req.nonceStr = json.getString("noncestr"); // 随机字符串,不长于32位
            req.timeStamp = json.getString("timestamp"); // 时间戳
            req.packageValue = json.getString("package"); // 扩展字段, 暂填写固定值Sign=WXPay
            req.sign = json.getString("sign"); // 签名
            return pay(req);
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean pay(PayReq payReq) {
        if (api == null) {
            LoggerUtil.e(TAG, "支付失败: 请先初始化后再试");
            return false;
        } else if (payReq == null) {
            LoggerUtil.e(TAG, "字符失败: PayReq is null");
            return false;
        }
        return api.sendReq(payReq);
    }

    public IWXAPI getWXApi() {
        return api;
    }
}