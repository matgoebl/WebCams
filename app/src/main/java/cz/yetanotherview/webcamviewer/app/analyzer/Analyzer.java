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

package cz.yetanotherview.webcamviewer.app.analyzer;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.os.AsyncTask;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.helper.LinkSizeComparator;
import cz.yetanotherview.webcamviewer.app.helper.Utils;
import cz.yetanotherview.webcamviewer.app.model.Link;

public class Analyzer {

    private final Context context;
	private final AnalyzerCallback callback;
    private AnalyzerAsyncTask mTask;

    private boolean complete;
    private static final int sleep = 10;
    private static final int trimEnd = 22;
    private static final int minAllowedWidth = 320;
    private static final int minAllowedFileSize = 30000;
    private static final String[] feedRules = {"rss","feed"};
    private static final String[] jpgRules = {".jpg",".jpeg"};
    private static final String[] imgRules = {".jpg",".cgi",".php",".asp"};
    private static final String[] linksRules = {"cam","big","kam","img"};
    private static final String[] notAllowed = {".png",".gif"};

	public Analyzer(Context context, AnalyzerCallback callback) {
        this.context = context;
		this.callback = callback;
	}

	public void startTask(String url, boolean complete) {
        this.complete = complete;
        mTask = new AnalyzerAsyncTask();
        mTask.execute(url);
	}

    public void stopTask() {
        if (mTask != null) {
            mTask.cancel(true);
        }
    }

	private class AnalyzerAsyncTask extends AsyncTask<String, String, List<Link>> {

        @Override
        protected List<Link> doInBackground(String... url) {

            List<Link> tmpLinks;

            if (complete) {
                try {
                    print("Fetching %s...", url[0]);

                    Document doc = Jsoup.connect(url[0]).get();
                    tmpLinks = imgLinksOnly(url[0]);

                    List<Link> tmpFeedLinks = imgFeedLinks(doc);
                    for (Link tmpFeedLink : tmpFeedLinks) {
                        tmpLinks.add(tmpFeedLink);
                    }

                    List<String> tmpPageLinks = linksStrings(doc);
                    for (String link : tmpPageLinks) {
                        List<Link> tmpImageLinks = imgLinksOnly(link);
                        for (Link tmpImageLink : tmpImageLinks) {
                            tmpLinks.add(tmpImageLink);
                        }
                    }

                    return cleanSizedSortedLinks(tmpLinks);
                }
                catch (IOException ignored) {
                    return null;
                } catch (InterruptedException e) {
                    return null;
                }
            }
            else {
                try {
                    tmpLinks = imgLinksOnly(url[0]);

                    return cleanSizedSortedLinks(tmpLinks);
                }
                catch (IOException ignored) {
                    return null;
                } catch (InterruptedException e) {
                    return null;
                }
            }
        }

        private List<Link> imgLinksOnly(String urlString) throws IOException, InterruptedException {
            List<Link> tmpImageLinks;
            print("Fetching %s...", urlString);

            Document doc = Jsoup.connect(urlString).get();
            Elements media = doc.select("[src]");

            print("\nMedia: (%d)", media.size());
            tmpImageLinks = new ArrayList<>();
            int i = 0;
            for (Element src : media) {
                if (src.tagName().equals("img")) {
                    Thread.sleep(sleep);
                    publishProgress(context.getString(R.string.processing_image) + "\n" + trimFromEnd(src.attr("abs:src"), trimEnd));
                    if (!Utils.stringContainsItem(src.attr("abs:src"), notAllowed)) {
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
                                tmpImageLinks.add(new Link(i, src.attr("abs:src"), width, height));
                            }
                        }
                        i++;
                        print(" * %s: <%s> %sx%s (%s)",
                                src.tagName(), src.attr("abs:src"), src.attr("width"), src.attr("height"),
                                trim(src.attr("alt"), 20));
                    }
                }
            }
            return tmpImageLinks;
        }

        private List<String> linksStrings(Document doc) throws IOException, InterruptedException {
            List<String> finalLinks = new ArrayList<>();
            Elements links = doc.select("a[href]");
            print("\nLinks: (%d)", links.size());
            for (Element link : links) {
                if (!link.attr("abs:href").isEmpty()) {
                    Thread.sleep(sleep);
                    publishProgress(context.getString(R.string.processing_link) + "\n" + trimFromEnd(link.attr("abs:href"), trimEnd));
                    if (Utils.stringContainsItem(link.attr("abs:href"), linksRules)) {
                        finalLinks.add(link.attr("abs:href"));
                        print(" * a: <%s>", link.attr("abs:href"));
                    }
                }
            }

            return finalLinks;
        }

        private List<Link> imgFeedLinks(Document doc) throws IOException, InterruptedException {
            List<Link> finalImgFeedLinks = new ArrayList<>();
            Elements imports = doc.select("link[href]");
            int i = 0;
            for (Element link : imports) {
                if (!link.attr("abs:href").isEmpty()) {
                    if (Utils.stringContainsItem(link.attr("abs:href"), feedRules)) {
                        print(" * link: <%s>", link.attr("abs:href"));

                        Document feedLink = Jsoup.connect(link.attr("abs:href")).get();
                        List<String> extractedUrls = extractUrls(feedLink.toString());
                        for (String extractedUrl : extractedUrls) {
                            Thread.sleep(sleep);
                            publishProgress(context.getString(R.string.processing_feed_url) + "\n" + trimFromEnd(extractedUrl, trimEnd));
                            if (Utils.stringContainsItem(extractedUrl, imgRules)) {
                                finalImgFeedLinks.add(new Link(i, extractedUrl, 0, 0));
                                System.out.println("Extracted Url: " + extractedUrl);
                                i++;
                            }
                        }
                    }
                }
            }

            return finalImgFeedLinks;
        }

        private List<Link> cleanSizedSortedLinks(List<Link> tmpLinks) throws IOException {
            List<Link> noDuplicates = new ArrayList<>();
            Set<String> titles = new HashSet<>();
            for (Link link : tmpLinks) {
                if (titles.add(link.getUrl())) {
                    noDuplicates.add(link);
                }
            }
            List<Link> finalLinks = new ArrayList<>();
            for (Link link : noDuplicates) {
                if (isCancelled()) break;
                URL testUrl = new URL(link.getUrl());
                URLConnection urlConnection = testUrl.openConnection();
                urlConnection.connect();
                int file_size = urlConnection.getContentLength();
                System.out.println("Fetching size: " + link.getUrl() + " " + file_size);
                publishProgress(context.getString(R.string.fetching_size) + "\n" + trimFromEnd(link.getUrl(), trimEnd));
                if (complete) {
                    if (Utils.stringContainsItem(link.getUrl(), jpgRules)) {
                        if (file_size >= minAllowedFileSize) {
                            link.setSize(file_size);
                            finalLinks.add(link);
                        }
                    }
                    else {
                        finalLinks.add(link);
                    }
                }
                else {
                    if (file_size >= minAllowedFileSize) {
                        link.setSize(file_size);
                        finalLinks.add(link);
                    }
                }
            }
            Collections.sort(finalLinks, new LinkSizeComparator());
            return finalLinks;
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            callback.onAnalyzingUpdate(progress[0]);
        }

        @Override
        protected void onPostExecute(List<Link> result) {
            callback.onAnalyzingCompleted(result, complete);
        }
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

    private static String trimFromEnd(String s, int width) {
        if (s.length() > width)
            return " ..." + s.substring(s.length()-width, s.length());
        else
            return s;
    }

    private static List<String> extractUrls(String text) {
        List<String> containedUrls = new ArrayList<>();
        String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = pattern.matcher(text);

        while (urlMatcher.find()) {
            containedUrls.add(text.substring(urlMatcher.start(0),
                    urlMatcher.end(0)));
        }

        return containedUrls;
    }
}
