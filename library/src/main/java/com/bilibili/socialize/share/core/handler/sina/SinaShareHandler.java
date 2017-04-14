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

package com.bilibili.socialize.share.core.handler.sina;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.bilibili.socialize.share.core.BiliShareConfiguration;
import com.bilibili.socialize.share.core.SharePlatformConfig;
import com.bilibili.socialize.share.core.SocializeListeners;
import com.bilibili.socialize.share.core.SocializeMedia;
import com.bilibili.socialize.share.core.error.BiliShareStatusCode;
import com.bilibili.socialize.share.core.error.InvalidParamException;
import com.bilibili.socialize.share.core.error.ShareConfigException;
import com.bilibili.socialize.share.core.error.ShareException;
import com.bilibili.socialize.share.core.error.UnSupportedException;
import com.bilibili.socialize.share.core.handler.BaseShareHandler;
import com.bilibili.socialize.share.core.helper.AccessTokenKeeper;
import com.bilibili.socialize.share.core.shareparam.BaseShareParam;
import com.bilibili.socialize.share.core.shareparam.ShareAudio;
import com.bilibili.socialize.share.core.shareparam.ShareImage;
import com.bilibili.socialize.share.core.shareparam.ShareParamAudio;
import com.bilibili.socialize.share.core.shareparam.ShareParamImage;
import com.bilibili.socialize.share.core.shareparam.ShareParamText;
import com.bilibili.socialize.share.core.shareparam.ShareParamVideo;
import com.bilibili.socialize.share.core.shareparam.ShareParamWebPage;
import com.bilibili.socialize.share.core.shareparam.ShareVideo;
import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.MusicObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.VideoObject;
import com.sina.weibo.sdk.api.WebpageObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.constant.WBConstants;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.utils.Utility;

import java.io.File;
import java.util.Map;

/**
 * 支持所有的类型。
 *
 * @author Jungly
 * @email jungly.ik@gmail.com
 * @since 2015/10/9
 */
public class SinaShareHandler extends BaseShareHandler {

    public static final String DEFAULT_REDIRECT_URL = "https://api.weibo.com/oauth2/default.html";
    public static final String DEFAULT_SCOPE = "email,direct_messages_read,direct_messages_write,"
            + "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
            + "follow_app_official_microblog," + "invitation_write";

    public IWeiboShareAPI mWeiboShareAPI = null;
    private SsoHandler mSsoHandler;
    private WeiboMultiMessage mWeiboMessage;
    private String mAppKey;

    public SinaShareHandler(Activity context, BiliShareConfiguration configuration) {
        super(context, configuration);
    }

    @Override
    public void checkConfig() throws Exception {
        if (!TextUtils.isEmpty(mAppKey)) {
            return;
        }

        Map<String, String> appConfig = mShareConfiguration.getPlatformConfig().getPlatformDevInfo(SocializeMedia.SINA);
        if (appConfig == null || TextUtils.isEmpty(mAppKey = appConfig.get(SharePlatformConfig.APP_KEY))) {
            throw new ShareConfigException("Please set Sina platform dev info.");
        }
    }

    @Override
    public void init() throws Exception {
        if (mWeiboShareAPI == null) {
            mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(getContext(), mAppKey);
            mWeiboShareAPI.registerApp();
        }
    }

    /**
     * 当 Activity 被重新初始化时（该 Activity 处于后台时，可能会由于内存不足被杀掉了），
     * 需要调用 {@link IWeiboShareAPI#handleWeiboResponse} 来接收微博客户端返回的数据。
     * 执行成功，返回 true，并调用 {@link IWeiboHandler.Response#onResponse}；
     * 失败返回 false，不调用上述回调
     *
     * @param activity
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState, SocializeListeners.ShareListener listener) {
        super.onActivityCreated(activity, savedInstanceState, listener);
        if (savedInstanceState != null && mWeiboShareAPI != null) {
            mWeiboShareAPI.handleWeiboResponse(activity.getIntent(), (IWeiboHandler.Response) activity);
        }
    }

    /**
     * 从当前应用唤起微博并进行分享后，返回到当前应用时，需要在此处调用该函数
     * 来接收微博客户端返回的数据；执行成功，返回 true，并调用
     * {@link IWeiboHandler.Response#onResponse}；失败返回 false，不调用上述回调
     *
     * @param intent
     */
    @Override
    public void onActivityNewIntent(Activity activity, Intent intent) {
        super.onActivityNewIntent(activity, intent);
        if (mWeiboShareAPI != null)
            try {
                mWeiboShareAPI.handleWeiboResponse(intent, (IWeiboHandler.Response) activity);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent
            data, SocializeListeners.ShareListener listener) {
        super.onActivityResult(activity, requestCode, resultCode, data, listener);
        if (mSsoHandler != null && TextUtils.isEmpty(getToken())) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }

    }

    /**
     * Contont cannot be empty
     *
     * @param params
     * @throws ShareException
     */
    @Override
    protected void shareText(final ShareParamText params) throws ShareException {
        checkContent(params);

        final WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
        weiboMessage.textObject = getTextObj(params);

        allInOneShare(weiboMessage);
    }

    /**
     * Contont and image cannot be empty or null
     *
     * @param params
     * @throws ShareException
     */
    @Override
    protected void shareImage(final ShareParamImage params) throws ShareException {
        checkContent(params);
        checkImage(params.getImage());

        doOnWorkThread(new Runnable() {
            @Override
            public void run() {
                final WeiboMultiMessage weiboMessage = new WeiboMultiMessage();

                weiboMessage.textObject = getTextObj(params);
                weiboMessage.imageObject = getImageObj(params.getImage());

                allInOneShare(weiboMessage);
            }
        });
    }

    /**
     * Contont and image cannot be empty or null
     *
     * @param params
     * @throws ShareException
     */
    @Override
    protected void shareWebPage(final ShareParamWebPage params) throws ShareException {
        checkContent(params);
        if (TextUtils.isEmpty(params.getTargetUrl())) {
            throw new InvalidParamException("Target url is empty or illegal");
        }

        doOnWorkThread(new Runnable() {
            @Override
            public void run() {
                final WeiboMultiMessage weiboMessage = new WeiboMultiMessage();

                if (!isSinaClientInstalled()) {
                    weiboMessage.textObject = getTextObj(params);
                }

                try {
                    checkImage(params.getThumb());
                    weiboMessage.imageObject = getImageObj(params.getThumb());
                } catch (Exception e) {
                    weiboMessage.textObject = getTextObj(params);
                }

                weiboMessage.mediaObject = getWebPageObj(params);

                allInOneShare(weiboMessage);
            }
        });
    }

    @Override
    protected void shareAudio(final ShareParamAudio params) throws ShareException {
        checkContent(params);
        if (TextUtils.isEmpty(params.getTargetUrl())) {
            throw new InvalidParamException("Target url is empty or illegal");
        }
        if (params.getAudio() == null) {
            throw new InvalidParamException("Audio is empty or illegal");
        }

        doOnWorkThread(new Runnable() {
            @Override
            public void run() {
                final WeiboMultiMessage weiboMessage = new WeiboMultiMessage();

                if (!isSinaClientInstalled()) {
                    weiboMessage.textObject = getTextObj(params);
                }

                try {
                    checkImage(params.getThumb());
                    weiboMessage.imageObject = getImageObj(params.getThumb());
                } catch (Exception e) {
                    weiboMessage.textObject = getTextObj(params);
                }

                weiboMessage.mediaObject = getAudioObj(params);

                allInOneShare(weiboMessage);
            }
        });
    }

    @Override
    protected void shareVideo(final ShareParamVideo params) throws ShareException {
        checkContent(params);
        if (TextUtils.isEmpty(params.getTargetUrl())) {
            throw new InvalidParamException("Target url is empty or illegal");
        }
        if (params.getVideo() == null) {
            throw new InvalidParamException("Video is empty or illegal");
        }

        doOnWorkThread(new Runnable() {
            @Override
            public void run() {
                final WeiboMultiMessage weiboMessage = new WeiboMultiMessage();

                if (!isSinaClientInstalled()) {
                    weiboMessage.textObject = getTextObj(params);
                }

                try {
                    checkImage(params.getThumb());
                    weiboMessage.imageObject = getImageObj(params.getThumb());
                } catch (Exception e) {
                    weiboMessage.textObject = getTextObj(params);
                }

                weiboMessage.mediaObject = getVideoObj(params);

                allInOneShare(weiboMessage);
            }
        });
    }

    private void checkContent(BaseShareParam params) throws ShareException {
        if (TextUtils.isEmpty(params.getContent())) {
            throw new InvalidParamException("Content is empty or illegal");
        }
    }

    private void checkImage(ShareImage image) throws ShareException {
        if (image == null) {
            throw new InvalidParamException("Image cannot be null");
        }

        if (image.isLocalImage()) {
            if (TextUtils.isEmpty(image.getLocalPath()) || !new File(image.getLocalPath()).exists()) {
                throw new InvalidParamException("Image path is empty or illegal");
            }
        } else if (image.isNetImage()) {
            if (TextUtils.isEmpty(image.getNetImageUrl())) {
                throw new InvalidParamException("Image url is empty or illegal");
            }
        } else if (image.isResImage())
            throw new UnSupportedException("Unsupport image type");
        else if (image.isBitmapImage()) {
            if (image.getBitmap().isRecycled()) {
                throw new InvalidParamException("Cannot share recycled bitmap.");
            }
        } else
            throw new UnSupportedException("Invaild image");
    }

    /**
     * 创建文本消息对象。
     *
     * @return 文本消息对象。
     */
    private TextObject getTextObj(BaseShareParam params) {
        TextObject textObject = new TextObject();

        if (params != null) {
            textObject.text = params.getContent();
        }

        return textObject;
    }

    /**
     * 创建图片消息对象。
     *
     * @return 图片消息对象。
     */
    private ImageObject getImageObj(ShareImage image) {
        ImageObject imageObject = new ImageObject();

        if (image == null) {
            return imageObject;
        }

        if (image.isLocalImage()) {
            imageObject.imagePath = image.getLocalPath();
        } else {
            imageObject.imageData = mImageHelper.buildThumbData(image);
        }
        return imageObject;
    }

    /**
     * 创建多媒体（网页）消息对象。
     *
     * @return 多媒体（网页）消息对象。
     */
    private WebpageObject getWebPageObj(ShareParamWebPage params) {
        WebpageObject mediaObject = new WebpageObject();
        mediaObject.identify = Utility.generateGUID();
        mediaObject.title = params.getContent();
        mediaObject.description = params.getTitle();

        byte[] thumbData = mImageHelper.buildThumbData(params.getThumb());
        if (thumbData == null || thumbData.length == 0) {
            mediaObject.thumbData = mImageHelper.buildThumbData(new ShareImage(mShareConfiguration.getDefaultShareImage()));
        } else
            mediaObject.thumbData = thumbData;

        mediaObject.actionUrl = params.getTargetUrl();
        mediaObject.defaultText = " 哔哩哔哩  ( ゜- ゜)つロ 乾杯~";
        return mediaObject;
    }

    /**
     * 创建多媒体（音乐）消息对象。
     *
     * @return 多媒体（音乐）消息对象。
     */
    private MusicObject getAudioObj(ShareParamAudio params) {
        MusicObject musicObject = new MusicObject();
        musicObject.identify = Utility.generateGUID();
        musicObject.title = params.getContent();
        musicObject.description = params.getTitle();

        byte[] thumbData = mImageHelper.buildThumbData(params.getThumb());
        if (thumbData == null || thumbData.length == 0) {
            musicObject.thumbData = mImageHelper.buildThumbData(new ShareImage(mShareConfiguration.getDefaultShareImage()));
        } else {
            musicObject.thumbData = thumbData;
        }

        musicObject.actionUrl = params.getTargetUrl();

        ShareAudio audio = params.getAudio();
        if (audio != null) {
            musicObject.dataUrl = audio.getAudioSrcUrl();
            musicObject.dataHdUrl = audio.getAudioSrcUrl();
            musicObject.h5Url = audio.getAudioH5Url();
            musicObject.duration = 10;
            musicObject.defaultText = audio.getDescription();
        }
        return musicObject;
    }

    /**
     * 创建多媒体（视频）消息对象。
     *
     * @return 多媒体（视频）消息对象。
     */
    private VideoObject getVideoObj(ShareParamVideo param) {
        VideoObject videoObject = new VideoObject();
        videoObject.identify = Utility.generateGUID();
        videoObject.title = param.getContent();
        videoObject.description = param.getTitle();

        byte[] thumbData = mImageHelper.buildThumbData(param.getThumb());
        if (thumbData == null || thumbData.length == 0) {
            videoObject.thumbData = mImageHelper.buildThumbData(new ShareImage(mShareConfiguration.getDefaultShareImage()));
        } else {
            videoObject.thumbData = thumbData;
        }

        videoObject.actionUrl = param.getTargetUrl();

        ShareVideo video = param.getVideo();
        if (video != null) {
            videoObject.dataUrl = video.getVideoSrcUrl();
            videoObject.dataHdUrl = video.getVideoSrcUrl();
            videoObject.h5Url = video.getVideoH5Url();
            videoObject.duration = 10;
            videoObject.defaultText = video.getDescription();
        }
        return videoObject;
    }

    private void allInOneShare(WeiboMultiMessage weiboMessage) {
        final SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.multiMessage = weiboMessage;

        final String token = getToken();
        Map<String, String> appConfig = mShareConfiguration.getPlatformConfig().getPlatformDevInfo(SocializeMedia.SINA);
        final AuthInfo mAuthInfo = new AuthInfo(getContext(), mAppKey,
                appConfig.get(SharePlatformConfig.REDIRECT_URL), appConfig.get(SharePlatformConfig.SCOPE));
        if (TextUtils.isEmpty(token)) {
            mWeiboMessage = weiboMessage;
            mSsoHandler = new SsoHandler((Activity) getContext(), mAuthInfo);
            mSsoHandler.authorize(mAuthListener);
        } else {
            mWeiboMessage = null;
            mSsoHandler = null;
            doOnMainThread(new Runnable() {
                @Override
                public void run() {
                    postProgressStart();
                    boolean result = mWeiboShareAPI.sendRequest((Activity) getContext(), request, mAuthInfo, token, mAuthListener);
                    if (!result) {
                        if (getShareListener() != null) {
                            getShareListener().onError(getShareMedia(), 0, null);
                        }
                    }
                }
            });
        }

    }

    private WeiboAuthListener mAuthListener = new WeiboAuthListener() {

        @Override
        public void onWeiboException(WeiboException arg0) {
            if (getShareListener() != null) {
                getShareListener().onError(SocializeMedia.SINA, BiliShareStatusCode.ST_CODE_SHARE_ERROR_AUTH_FAILED, arg0);
            }
        }

        @Override
        public void onComplete(Bundle bundle) {
            Oauth2AccessToken newToken = Oauth2AccessToken.parseAccessToken(bundle);
            if (newToken.isSessionValid()) {
                AccessTokenKeeper.writeAccessToken(getContext(), newToken);
                if (mWeiboMessage != null) {
                    allInOneShare(mWeiboMessage);
                }
                return;
            }

            SocializeListeners.ShareListener listener = getShareListener();
            if (listener == null) {
                return;
            }

            listener.onError(SocializeMedia.SINA, BiliShareStatusCode.ST_CODE_SHARE_ERROR_AUTH_FAILED, new ShareException("无效的token"));
        }

        @Override
        public void onCancel() {
            if (getShareListener() != null) {
                getShareListener().onCancel(SocializeMedia.SINA);
            }
        }
    };

    public void onResponse(BaseResponse baseResp) {
        SocializeListeners.ShareListener listener = getShareListener();
        if (listener == null) {
            return;
        }

        switch (baseResp.errCode) {
            case WBConstants.ErrorCode.ERR_OK:
                listener.onSuccess(SocializeMedia.SINA, BiliShareStatusCode.ST_CODE_SUCCESSED);
                break;
            case WBConstants.ErrorCode.ERR_CANCEL:
                listener.onCancel(SocializeMedia.SINA);
                break;
            case WBConstants.ErrorCode.ERR_FAIL:
                listener.onError(SocializeMedia.SINA, BiliShareStatusCode.ST_CODE_SHARE_ERROR_SHARE_FAILED, new ShareException(baseResp.errMsg));
                break;
        }
    }

    private String getToken() {
        Oauth2AccessToken mAccessToken = AccessTokenKeeper.readAccessToken(getContext());
        String token = null;
        if (mAccessToken != null) {
            token = mAccessToken.getToken();
        }
        return token;
    }

    @Override
    public void release() {
        super.release();
        mSsoHandler = null;
        mWeiboShareAPI = null;
        mWeiboMessage = null;
    }

    private boolean isSinaClientInstalled() {
        return mWeiboShareAPI != null && mWeiboShareAPI.isWeiboAppInstalled();
    }

    @Override
    protected boolean isNeedActivityContext() {
        return true;
    }

    @Override
    public SocializeMedia getShareMedia() {
        return SocializeMedia.SINA;
    }

}
