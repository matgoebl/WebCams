/*
* ******************************************************************************
* Copyright (c) 2013-2015 Tomas Valenta.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* *****************************************************************************
*/

package cz.yetanotherview.webcamviewer.app.helper;

import android.text.TextUtils;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;

public class HttpHeader {

    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:39.0) Gecko/20100101 Firefox/39.0";
    public static final String APP_AGENT = "0I9BttDa88rL";

    public static GlideUrl getUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        else return new GlideUrl(url, new LazyHeaders.Builder()
                .setHeader("User-Agent", USER_AGENT) // ToDo: Workaround for Glide bug #546
                .addHeader("App-agent", APP_AGENT)
                .build());
    }
}
