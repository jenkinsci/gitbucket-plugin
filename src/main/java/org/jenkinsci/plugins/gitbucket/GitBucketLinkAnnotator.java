/*
 * The MIT License
 *
 * Copyright (c) 2013, Seiji Sogabe
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugins.gitbucket;

import hudson.Extension;
import hudson.MarkupText;
import hudson.MarkupText.SubText;
import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogAnnotator;
import hudson.scm.ChangeLogSet;
import java.util.regex.Pattern;

/**
 * Creates HTML link for GitBucket issues.
 *
 * @author sogabe
 */
@Extension
public class GitBucketLinkAnnotator extends ChangeLogAnnotator {

    @Override
    public void annotate(AbstractBuild<?, ?> build, ChangeLogSet.Entry change, MarkupText text) {
        GitBucketProjectProperty gpp = GitBucketProjectProperty.get(build);
        if (gpp == null) {
            return;
        }
        if (!gpp.isLinkEnabled()) {
            return;
        }

        String url = gpp.getUrl();
        annotate(text, url);
    }

    void annotate(MarkupText text, String url) {
        for (LinkMarkup markup : MARKUPS) {
            markup.process(text, url);
        }
    }

    private static final LinkMarkup[] MARKUPS = new LinkMarkup[]{
        new LinkMarkup("refs\\s+#?(\\d+)", "issues/$1"),
        new LinkMarkup("issue\\s+#?(\\d+)", "issues/$1"),
        new LinkMarkup("pull\\s+#?(\\d+)", "pulls/$1"),
        new LinkMarkup("wiki\\s+(\\w+)", "wiki/$1")
    };

    private static final class LinkMarkup {

        private final Pattern pattern;

        private final String href;

        LinkMarkup(String pattern, String href) {
            this.pattern = Pattern.compile(pattern);
            this.href = href;
        }

        void process(MarkupText text, String url) {
            for (SubText st : text.findTokens(pattern)) {
                st.surroundWith(
                        "<a href='" + url + href + "'>",
                        "</a>");
            }
        }
    }
}
