package org.lynxz.basepaywrapper.bizlooper;

import android.os.Message;

public interface IBizHandler {

    /**
     * 处理biz消息
     * 回抛到调用方的 message 都是不带 callback 的, 需要根据 msg.what 进行区别处理
     *
     * @return 消息是否已被消费
     */
    boolean onHandleBizMessage(Message msg);
}
