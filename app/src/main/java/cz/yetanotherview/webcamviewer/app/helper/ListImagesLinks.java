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
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cz.yetanotherview.webcamviewer.app.model.Link;

public class ListImagesLinks {

    private static final int minAllowedWidth = 320;
    private static final int minAllowedFileSize = 30000;

    public static List<Link> main(String url) throws IOException {
        print("Fetching %s...", url);

        Document doc = Jsoup.connect(url).get();
        Elements media = doc.select("[src]");

        print("\nMedia: (%d)", media.size());
        List<Link> tmpLinks = new ArrayList<>();
        int i = 0;
        for (Element src : media) {
            if (src.tagName().equals("img")) {
                if (!src.attr("abs:src").contains(".png") && !src.attr("abs:src").contains(".gif")) {
                    String widthString = src.attr("width");
                    int width;
                    if (!widthString.isEmpty()) {
                        width = Integer.parseInt(src.attr("width"));
                    }
                    else width = 0;
                    String heightString = src.attr("height");
                    int height;
                    if (!heightString.isEmpty()) {
                        height = Integer.parseInt(src.attr("height"));
                    }
                    else height = 0;

                    if (width == 0 || width >= minAllowedWidth) {
                        if (!src.attr("abs:src").isEmpty()) {
                            tmpLinks.add(new Link(i, src.attr("abs:src"), width, height));
                        }
                    }
                    i++;
                    print(" * %s: <%s> %sx%s (%s)",
                            src.tagName(), src.attr("abs:src"), src.attr("width"), src.attr("height"),
                            trim(src.attr("alt"), 20));
                }
            }
        }

        List<Link> noDuplicates = new ArrayList<>();
        Set<String> titles = new HashSet<>();
        for (Link link : tmpLinks ) {
            if (titles.add(link.getUrl())) {
                noDuplicates.add(link);
            }
        }

        List<Link> finalLinks = new ArrayList<>();
        for (Link link : noDuplicates) {
            URL testUrl = new URL(link.getUrl());
            URLConnection urlConnection = testUrl.openConnection();
            urlConnection.connect();
            int file_size = urlConnection.getContentLength();
            System.out.println("Fetching size: " + link.getUrl() + " " + file_size);
            if (file_size >= minAllowedFileSize) {
                link.setSize(file_size);
                finalLinks.add(link);
            }
        }
        Collections.sort(finalLinks, new LinkSizeComparator());

//        Elements linksTT = doc.select("a[href]");
//        print("\nLinks: (%d)", links.size());
//        for (Element link : linksTT) {
//            print(" * a: <%s>  (%s)", link.attr("abs:href"), trim(link.text(), 35));
//        }

        return finalLinks;
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
