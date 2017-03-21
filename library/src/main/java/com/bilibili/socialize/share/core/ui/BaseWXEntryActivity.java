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

package com.bilibili.socialize.share.core.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.bilibili.socialize.share.core.error.BiliShareStatusCode;
import com.bilibili.socialize.share.core.handler.wx.BaseWxShareHandler;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

/**
 * @author Jungly
 * @email jungly.ik@gmail.com
 * @date 2015/10/8
 */
public abstract class BaseWXEntryActivity extends Activity implements IWXAPIEventHandler {

    private IWXAPI mIWXAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isAutoCreateWXAPI() && mIWXAPI == null) {
            initWXApi();
        }
    }

    private void initWXApi() {
        mIWXAPI = WXAPIFactory.createWXAPI(this, getAppId(), true);
        if (mIWXAPI.isWXAppInstalled()) {
            mIWXAPI.registerApp(getAppId());
        }
        mIWXAPI.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
        if (mIWXAPI != null) {
            mIWXAPI.handleIntent(intent, this);
        }
    }

    @Override
    public void onReq(BaseReq baseReq) {
        if (isAutoFinishAfterOnReq()) {
            finish();
        }
    }

    @Override
    public void onResp(BaseResp resp) {
        parseResult(resp);
        if (isAutoFinishAfterOnResp()) {
            finish();
        }
    }

    private void parseResult(BaseResp resp) {
        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                sendResult(BiliShareStatusCode.ST_CODE_SUCCESSED, null);
                break;

            case BaseResp.ErrCode.ERR_USER_CANCEL:
                sendResult(BiliShareStatusCode.ST_CODE_ERROR_CANCEL, null);
                break;

            case BaseResp.ErrCode.ERR_SENT_FAILED:
                sendResult(BiliShareStatusCode.ST_CODE_ERROR, resp.errStr);
                break;
        }
    }

    private void sendResult(int statusCode, String msg) {
        Intent intent = new Intent(BaseWxShareHandler.ACTION_RESULT);
        intent.putExtra(BaseWxShareHandler.BUNDLE_STATUS_CODE, statusCode);
        intent.putExtra(BaseWxShareHandler.BUNDLE_STATUS_MSG, msg);
        sendBroadcast(intent);
    }

    protected boolean isAutoFinishAfterOnReq() {
        return true;
    }

    protected boolean isAutoFinishAfterOnResp() {
        return true;
    }

    protected boolean isAutoCreateWXAPI() {
        return true;
    }

    protected abstract String getAppId();

}
