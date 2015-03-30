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

public class Type {

    private int id;
    private int icon;
    private String icon_name;
    private String type_name;
    private int count;

    // constructors
    public Type() {

    }

    public Type(String icon_name, String type_name) {
        this.icon_name = icon_name;
        this.type_name = type_name;
    }

    public Type(int id, String icon_name, String type_name) {
        this.id = id;
        this.icon_name = icon_name;
        this.type_name = type_name;
    }

    // setter
    public void setId(int id) {
        this.id = id;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public void setIconName(String icon_name) {
        this.icon_name = icon_name;
    }

    public void setTypeName(String type_name) {
        this.type_name = type_name;
    }

    public void setCount(int count) {
        this.count = count;
    }

    // getter
    public int getId() {
        return this.id;
    }

    public int getIcon() {
        return this.icon;
    }

    public String getIconName() {
        return this.icon_name;
    }

    public String getTypeName() {
        return this.type_name;
    }

    public int getCount() {
        return this.count;
    }

    public String getCountAsString() {
        return String.valueOf(this.count);
    }

}
