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
        annotate(url, text);
    }

    void annotate(String url, MarkupText text) {
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
                        "<a href='" + url + '/' + href + "'>",
                        "</a>");
            }
        }
    }
}
