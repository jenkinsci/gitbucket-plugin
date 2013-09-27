package org.jenkinsci.plugins.gitbucket;

import hudson.MarkupText;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import static org.junit.Assert.*;
import org.junit.Test;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GitBucketLinkAnnotatorTest {

    private static final String GITBUCKET_URL = "http://bacons.ddo.jp/gitbucket/jenkins/gitbucket-plugin/";

    @Test
    public void testAnnoate() {
        AbstractProject job = mock(FreeStyleProject.class);
        AbstractBuild build = mock(FreeStyleBuild.class);
        GitBucketProjectProperty gpp =  new GitBucketProjectProperty(GITBUCKET_URL, true);
        when(build.getProject()).thenReturn(job);
        when(job.getProperty(GitBucketProjectProperty.class)).thenReturn(gpp);
        
        MarkupText text = mock(MarkupText.class);
                
        GitBucketLinkAnnotator target = spy(new GitBucketLinkAnnotator());
        target.annotate(build, null, text);

        verify(target, times(1)).annotate(text, GITBUCKET_URL);
    }
    
    @Test
    public void testAnnoate_NoProjectProperty() {
        AbstractProject job = mock(FreeStyleProject.class);
        AbstractBuild build = mock(FreeStyleBuild.class);
        GitBucketProjectProperty gpp = null;
        when(build.getProject()).thenReturn(job);
        when(job.getProperty(GitBucketProjectProperty.class)).thenReturn(gpp);
        
        MarkupText text = mock(MarkupText.class);
                
        GitBucketLinkAnnotator target = spy(new GitBucketLinkAnnotator());
        target.annotate(build, null, text);

        verify(target, never()).annotate(eq(text), anyString());
    }

    @Test
    public void testAnnoate_LinkDisabled() {
        AbstractProject job = mock(FreeStyleProject.class);
        AbstractBuild build = mock(FreeStyleBuild.class);
        GitBucketProjectProperty gpp = new GitBucketProjectProperty(GITBUCKET_URL, false);
        when(build.getProject()).thenReturn(job);
        when(job.getProperty(GitBucketProjectProperty.class)).thenReturn(gpp);
        
        MarkupText text = mock(MarkupText.class);
                
        GitBucketLinkAnnotator target = spy(new GitBucketLinkAnnotator());
        target.annotate(build, null, text);

        verify(target, never()).annotate(eq(text), anyString());
    }
    
    @Test
    public void testAnnotateIssueMarkupText() {
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
    public void testAnnotatePullsMarkupText() {
        assertAnnotatedTextEquals(
                "pull #1 Fixed typo.",
                "<a href='" + GITBUCKET_URL + "pulls/1'>pull #1</a> Fixed typo.");
        assertAnnotatedTextEquals(
                "pull 100 Fixed typo.",
                "<a href='" + GITBUCKET_URL + "pulls/100'>pull 100</a> Fixed typo.");
    }

    @Test
    public void testAnnotateWikiMarkupText() {
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