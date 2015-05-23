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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cz.yetanotherview.webcamviewer.app.model.Link;

public class ListImagesLinks {
    public static List<Link> main(String url) throws IOException {
        print("Fetching %s...", url);

        Document doc = Jsoup.connect(url).get();
        Elements media = doc.select("[src]");

        print("\nMedia: (%d)", media.size());
        List<Link> links = new ArrayList<>();
        int i = 0;
        for (Element src : media) {
            if (src.tagName().equals("img")) {
                if (!src.attr("abs:src").contains(".png") && !src.attr("abs:src").contains(".gif")) {
                    links.add(new Link(i,src.attr("abs:src")));
                    i++;
                    print(" * %s: <%s> %sx%s (%s)",
                            src.tagName(), src.attr("abs:src"), src.attr("width"), src.attr("height"),
                            trim(src.attr("alt"), 20));
                }
            }
        }
        return links;
    }

    private static void print(String msg, Object... args) {
        System.out.println(String.format(msg, args));
    }

    private static String trim(String s, int width) {
        if (s.length() > width)
            return s.substring(0, width-1) + ".";
        else
            return s;
    }
}
