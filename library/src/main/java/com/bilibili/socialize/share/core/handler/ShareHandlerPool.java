/*
 * Copyright (C) 2015 Bilibili <jungly.ik@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bilibili.socialize.share.core.handler;

import android.app.Activity;

import com.bilibili.socialize.share.core.SocializeMedia;
import com.bilibili.socialize.share.core.BiliShareConfiguration;
import com.bilibili.socialize.share.core.handler.generic.CopyShareHandler;
import com.bilibili.socialize.share.core.handler.generic.GenericShareHandler;
import com.bilibili.socialize.share.core.handler.qq.QQChatShareHandler;
import com.bilibili.socialize.share.core.handler.qq.QQZoneShareHandler;
import com.bilibili.socialize.share.core.handler.sina.SinaShareTransitHandler;
import com.bilibili.socialize.share.core.handler.wx.WxChatShareHandler;
import com.bilibili.socialize.share.core.handler.wx.WxMomentShareHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Jungly
 * @email jungly.ik@gmail.com
 * @since 2015/10/12
 */
public class ShareHandlerPool {

    private static ShareHandlerPool ourInstance = new ShareHandlerPool();
    private Map<SocializeMedia, IShareHandler> mHandlerMap = new HashMap<>();

    private ShareHandlerPool() {
    }

    public static IShareHandler newHandler(Activity context, SocializeMedia type, BiliShareConfiguration shareConfiguration) {
        IShareHandler handler = null;
        switch (type) {
            case WEIXIN:
                handler = new WxChatShareHandler(context, shareConfiguration);
                break;

            case WEIXIN_MONMENT:
                handler = new WxMomentShareHandler(context, shareConfiguration);
                break;

            case QQ:
                handler = new QQChatShareHandler(context, shareConfiguration);
                break;

            case QZONE:
                handler = new QQZoneShareHandler(context, shareConfiguration);
                break;

            case SINA:
                handler = new SinaShareTransitHandler(context, shareConfiguration);
                break;

            case COPY:
                handler = new CopyShareHandler(context, shareConfiguration);
                break;

            default:
                handler = new GenericShareHandler(context, shareConfiguration);
        }

        ourInstance.mHandlerMap.put(type, handler);

        return handler;
    }

    public static IShareHandler getCurrentHandler(SocializeMedia type) {
        return ourInstance.mHandlerMap.get(type);
    }

    public static void remove(SocializeMedia type) {
        ourInstance.mHandlerMap.remove(type);
    }

}
