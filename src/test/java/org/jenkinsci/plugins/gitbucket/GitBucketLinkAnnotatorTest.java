package org.jenkinsci.plugins.gitbucket;

import hudson.MarkupText;
import static org.junit.Assert.*;
import org.junit.Test;

public class GitBucketLinkAnnotatorTest {

    private static final String GITBUCKET_URL = "http://bacons.ddo.jp/gitbucket/jenkins/gitbucket-plugin/";

    @Test
    public final void testAnnotateIssueMarkupText() {
        assertAnnotatedTextEquals(
                "(refs #1) Fixed XSS.",
                "(<a href='" + GITBUCKET_URL + "issues/1'>refs #1</a>) Fixed XSS.");
        assertAnnotatedTextEquals(
                "(refs 1) Fixed XSS.",
                "(<a href='" + GITBUCKET_URL + "issues/1'>refs 1</a>) Fixed XSS.");
        assertAnnotatedTextEquals(
                "(issue #100) Fixed XSS.",
                "(<a href='" + GITBUCKET_URL + "issues/100'>issue #100</a>) Fixed XSS.");
        assertAnnotatedTextEquals(
                "(issue 100) Fixed XSS.",
                "(<a href='" + GITBUCKET_URL + "issues/100'>issue 100</a>) Fixed XSS.");
    }

    @Test
    public final void testAnnotatePullsMarkupText() {
        assertAnnotatedTextEquals(
                "pull #1 Fixed typo.",
                "<a href='" + GITBUCKET_URL + "pulls/1'>pull #1</a> Fixed typo.");
        assertAnnotatedTextEquals(
                "pull 100 Fixed typo.",
                "<a href='" + GITBUCKET_URL + "pulls/100'>pull 100</a> Fixed typo.");
    }

    @Test
    public final void testAnnotateWikiMarkupText() {
        assertAnnotatedTextEquals(
                "wiki maven",
                "<a href='" + GITBUCKET_URL + "wiki/maven'>wiki maven</a>");
        assertAnnotatedTextEquals(
                "wiki GitBucket is a Github clone.",
                "<a href='" + GITBUCKET_URL + "wiki/GitBucket'>wiki GitBucket</a> is a Github clone.");
    }

    private void assertAnnotatedTextEquals(String originalText, String expectedAnnotatedText) {
        MarkupText markupText = new MarkupText(originalText);
        GitBucketLinkAnnotator annotator = new GitBucketLinkAnnotator();
        annotator.annotate(markupText, GITBUCKET_URL);
        assertEquals(expectedAnnotatedText, markupText.toString());
    }
}