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

import java.util.Date;

public class WebCam {

    private long id;
    private long uniId;
    private String webCamName;
    private String webCamTags;
    private String webCamUrl;
    private int position;
    private int status;
    private double latitude;
    private double longitude;
    private String country;
    private boolean popular;
    private Date dateModified;
    private String created_at;
    private boolean selected;

    // constructors
    public WebCam() {
    }

    public WebCam(String webCamName, String webCamUrl, int position, int status) {
        this.webCamName = webCamName;
        this.webCamUrl = webCamUrl;
        this.position = position;
        this.status = status;
    }

    public WebCam(String webCamName, String webCamUrl, int position, int status, double latitude, double longitude, Date dateModified) {
        this.webCamName = webCamName;
        this.webCamUrl = webCamUrl;
        this.position = position;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
        this.dateModified = dateModified;
    }

    public WebCam(long id, String webCamName, String webCamUrl, int position, int status) {
        this.id = id;
        this.webCamName = webCamName;
        this.webCamUrl = webCamUrl;
        this.position = position;
        this.status = status;
    }

    // setters
    public void setId(long id) {
        this.id = id;
    }

    public void setUniId(long uniId) {
        this.uniId = uniId;
    }

    public void setName(String webCamName) {
        this.webCamName = webCamName;
    }

    public void setTags(String webCamTags) {
        this.webCamTags = webCamTags;
    }

    public void setUrl(String webCamUrl) {
        this.webCamUrl = webCamUrl;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setPopular(boolean popular) {
        this.popular = popular;
    }

    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }

    public void setCreatedAt(String created_at){
        this.created_at = created_at;
    }

    public void setSelected(boolean selected)
    {
        this.selected = selected;
    }

    // getters
    public long getId() {
        return this.id;
    }

    public long getUniId() {
        return this.uniId;
    }

    public String getName() {
        return this.webCamName;
    }

    public String getTags() {
        return this.webCamTags;
    }

    public String getUrl() {
        return this.webCamUrl;
    }

    public int getPosition() {
        return this.position;
    }

    public int getStatus() {
        return this.status;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public String getCountry() {
        return this.country;
    }

    public boolean isPopular() {
        return this.popular;
    }

    public long getDateModifiedMillisecond () {
        if (dateModified != null) {
            return this.dateModified.getTime();
        }
        else return 0;
    }

    public long getDateModifiedFromDb() {
        if (dateModified != null) {
            return this.dateModified.getTime();
        }
        else return 0;
    }

    public String getCreatedAt() {
        return this.created_at;
    }

    public boolean isSelected()
    {
        return this.selected;
    }
}
