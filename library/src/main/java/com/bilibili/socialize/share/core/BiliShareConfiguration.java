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

package com.bilibili.socialize.share.core;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.bilibili.socialize.share.R;
import com.bilibili.socialize.share.core.handler.sina.SinaShareHandler;
import com.bilibili.socialize.share.download.DefaultImageDownloader;
import com.bilibili.socialize.share.download.IImageDownloader;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author Jungly
 * @email jungly.ik@gmail.com
 * @since 2015/10/8
 */
public class BiliShareConfiguration {

    String mImageCachePath;
    final int mDefaultShareImage;

    private String mSinaRedirectUrl;
    private String mSinaScope;

    final IImageDownloader mImageDownloader;
    final Executor mTaskExecutor;

    private BiliShareConfiguration(Builder builder) {
        mImageCachePath = builder.mImageCachePath;
        mDefaultShareImage = builder.mDefaultShareImage;
        mSinaRedirectUrl = builder.mSinaRedirectUrl;
        mSinaScope = builder.mSinaScope;
        mImageDownloader = builder.mImageLoader;
        mTaskExecutor = Executors.newCachedThreadPool();
    }

    public String getImageCachePath(Context context) {
        if (TextUtils.isEmpty(mImageCachePath)) {
            mImageCachePath = Builder.getDefaultImageCacheFile(context.getApplicationContext());
        }
        return mImageCachePath;
    }

    public int getDefaultShareImage() {
        return mDefaultShareImage;
    }

    public String getSinaRedirectUrl() {
        return mSinaRedirectUrl;
    }

    public String getSinaScope() {
        return mSinaScope;
    }

    public IImageDownloader getImageDownloader() {
        return mImageDownloader;
    }

    public Executor getTaskExecutor() {
        return mTaskExecutor;
    }

    public static class Builder {
        public static final String IMAGE_CACHE_FILE_NAME = "shareImage";

        private Context mContext;
        private String mImageCachePath;
        private int mDefaultShareImage = -1;

        private String mSinaRedirectUrl;
        private String mSinaScope;

        private IImageDownloader mImageLoader;

        public Builder(Context context) {
            mContext = context.getApplicationContext();
        }

        public Builder imageCachePath(String path) {
            mImageCachePath = path;
            return this;
        }

        public Builder defaultShareImage(int defaultImage) {
            mDefaultShareImage = defaultImage;
            return this;
        }

        public Builder sina(String redirectUrl, String mScope) {
            mSinaRedirectUrl = redirectUrl;
            mSinaScope = mScope;
            return this;
        }

        public Builder imageDownloader(IImageDownloader loader) {
            mImageLoader = loader;
            return this;
        }

        public BiliShareConfiguration build() {
            checkFields();
            return new BiliShareConfiguration(this);
        }

        private void checkFields() {
            File imageCacheFile = null;
            if (!TextUtils.isEmpty(mImageCachePath)) {
                imageCacheFile = new File(mImageCachePath);
                if (!imageCacheFile.isDirectory()) {
                    imageCacheFile = null;
                } else if (!imageCacheFile.exists() && !imageCacheFile.mkdirs()) {
                    imageCacheFile = null;
                }
            }
            if (imageCacheFile == null) {
                mImageCachePath = getDefaultImageCacheFile(mContext);
            }

            if (mImageLoader == null) {
                mImageLoader = new DefaultImageDownloader();
            }

            if (mDefaultShareImage == -1) {
                mDefaultShareImage = R.drawable.default_share_image;
            }

            if (TextUtils.isEmpty(mSinaRedirectUrl)) {
                mSinaRedirectUrl = SinaShareHandler.DEFAULT_REDIRECT_URL;
            }

            if (TextUtils.isEmpty(mSinaScope)) {
                mSinaScope = SinaShareHandler.DEFAULT_SCOPE;
            }
        }

        private static String getDefaultImageCacheFile(Context context) {
            String imageCachePath = null;
            File extCacheFile = context.getExternalCacheDir();
            if (extCacheFile == null) {
                extCacheFile = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            }
            if (extCacheFile != null) {
                imageCachePath = extCacheFile.getAbsolutePath() + File.separator + IMAGE_CACHE_FILE_NAME + File.separator;
                File imageCacheFile = new File(imageCachePath);
                imageCacheFile.mkdirs();
            }
            return imageCachePath;
        }
    }

}
