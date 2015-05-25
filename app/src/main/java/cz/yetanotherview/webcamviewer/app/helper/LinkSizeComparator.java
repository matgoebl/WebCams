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

import java.util.Comparator;

import cz.yetanotherview.webcamviewer.app.model.Link;

public class LinkSizeComparator implements Comparator<Link> {
    @Override
    public int compare(Link link1, Link link2) {
        Long sizeLink1 = link1.getSize();
        Long sizeLink2 = link2.getSize();

        return sizeLink2.compareTo(sizeLink1);
    }
}
