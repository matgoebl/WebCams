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

package cz.yetanotherview.webcamviewer.app.model;

import cz.yetanotherview.webcamviewer.app.helper.Utils;

public class Link {

    private long id;
    private String url;
    private int width, height;
    private long size;

    // constructors
    public Link(long id, String url, int width, int height) {
        this.id = id;
        this.url = url;
        this.width = width;
        this.height = height;
    }

    // setter
    public void setId(long id) {
        this.id = id;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setSize(long size) {
        this.size = size;
    }

    // getter
    public long getId() {
        return this.id;
    }

    public String getUrl() {
        return this.url;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public String getWidthAndHeightString() {
        if (width + height == 0) {
            return null;
        }
        else return String.valueOf(this.width + "x" + this.height);
    }

    public long getSize() {
        return this.size;
    }

    public String getSizeString() {
        return Utils.humanReadableByteCount(this.size, true);
    }
}
