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

import hudson.model.FreeStyleProject;
import hudson.plugins.git.GitSCM;
import hudson.scm.NullSCM;
import hudson.scm.SCM;
import java.util.Arrays;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.multiplescms.MultiSCM;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.kohsuke.stapler.StaplerRequest;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test for {@link GitBucketWebHook} class.
 *
 * @author sogabe
 */
public class GitBucketWebHookTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void testPushTrigger_GitSCM() throws Exception {
        // Repository URL
        String repo = j.createTmpDir().getAbsolutePath();

        // Setup FreeStyle Project
        FreeStyleProject fsp = j.createFreeStyleProject("GitSCM Project");

        // Setup Trigger
        GitBucketPushTrigger trigger = mock(GitBucketPushTrigger.class);
        fsp.addTrigger(trigger);

        // Setup SCM
        SCM scm = new GitSCM(repo);
        fsp.setScm(scm);

        // Setup WebHook request
        String payload = createPayload(repo, "jenkins");
        StaplerRequest req = mock(StaplerRequest.class);
        when(req.getParameter("payload")).thenReturn(payload);

        // Post WebHook
        GitBucketWebHook hook = new GitBucketWebHook();
        hook.doIndex(req);

        verify(trigger, times(1)).onPost((GitBucketPushRequest) anyObject());
    }

    @Test
    public void testPushTrigger_GitSCM_NoRepositoryUrl() throws Exception {
        // Repository URL
        String repo = j.createTmpDir().getAbsolutePath();

        // Setup FreeStyle Project
        FreeStyleProject fsp = j.createFreeStyleProject("GitSCM Project");

        // Setup Trigger
        GitBucketPushTrigger trigger = mock(GitBucketPushTrigger.class);
        fsp.addTrigger(trigger);

        // Setup SCM
        SCM scm = new GitSCM(repo);
        fsp.setScm(scm);

        // Setup WebHook request
        String payload = createPayload(null, "jenkins");
        StaplerRequest req = mock(StaplerRequest.class);
        when(req.getParameter("payload")).thenReturn(payload);

        // Post WebHook
        GitBucketWebHook hook = new GitBucketWebHook();
        hook.doIndex(req);

        verify(trigger, never()).onPost((GitBucketPushRequest) anyObject());
    }

    /**
     * compatibility test.
     *
     * GitBucket 1.7 or before has not pusher information in WebHook.
     */
    @Test
    public void testPushTrigger_GitSCM_NoPusher() throws Exception {
        // Repository URL
        String repo = j.createTmpDir().getAbsolutePath();

        // Setup FreeStyle Project
        FreeStyleProject fsp = j.createFreeStyleProject("GitSCM Project");

        // Setup Trigger
        GitBucketPushTrigger trigger = mock(GitBucketPushTrigger.class);
        fsp.addTrigger(trigger);

        // Setup SCM
        SCM scm = new GitSCM(repo);
        fsp.setScm(scm);

        // Setup WebHook request
        String payload = createPayload(repo, null);
        StaplerRequest req = mock(StaplerRequest.class);
        when(req.getParameter("payload")).thenReturn(payload);

        // Post WebHook
        GitBucketWebHook hook = new GitBucketWebHook();
        hook.doIndex(req);

        verify(trigger, times(1)).onPost((GitBucketPushRequest) anyObject());
    }

    @Test
    public void testPushTrigger_NoMatchRepo() throws Exception {
        // Repository URL
        String repo = j.createTmpDir().getAbsolutePath();

        // Setup FreeStyle Project
        FreeStyleProject fsp = j.createFreeStyleProject("GitSCM Project");

        // Setup Trigger
        GitBucketPushTrigger trigger = mock(GitBucketPushTrigger.class);
        fsp.addTrigger(trigger);

        // Setup SCM
        SCM scm = new GitSCM(repo);
        fsp.setScm(scm);

        // Setup WebHook request
        String payload = createPayload("No Match Repository", "jenkins");
        StaplerRequest req = mock(StaplerRequest.class);
        when(req.getParameter("payload")).thenReturn(payload);

        // Post WebHook
        GitBucketWebHook hook = new GitBucketWebHook();
        hook.doIndex(req);

        // make sure that onPost() never  called.
        verify(trigger, never()).onPost((GitBucketPushRequest) anyObject());
    }

    @Test
    public void testPushTrigger_NoTrigger() throws Exception {
        // Repository URL
        String repo = j.createTmpDir().getAbsolutePath();

        // Setup FreeStyle Project
        FreeStyleProject fsp = j.createFreeStyleProject("GitSCM Project");

        // Setup Trigger(No Trigger)
        // Setup SCM
        SCM scm = new GitSCM(repo);
        fsp.setScm(scm);

        // Setup WebHook request
        String payload = createPayload(repo, "jenkins");
        StaplerRequest req = mock(StaplerRequest.class);
        when(req.getParameter("payload")).thenReturn(payload);

        // Post WebHook
        GitBucketWebHook hook = new GitBucketWebHook();
        hook.doIndex(req);
    }

    @Test
    public void testPushTrigger_NoSCM() throws Exception {
        // Repository URL
        String repo = j.createTmpDir().getAbsolutePath();

        // Setup FreeStyle Project
        FreeStyleProject fsp = j.createFreeStyleProject("GitSCM Project");

        // Setup Trigger
        GitBucketPushTrigger trigger = mock(GitBucketPushTrigger.class);
        fsp.addTrigger(trigger);

        // Setup SCM (No SCM)
        // Setup WebHook request
        String payload = createPayload(repo, "jenkins");
        StaplerRequest req = mock(StaplerRequest.class);
        when(req.getParameter("payload")).thenReturn(payload);

        // Post WebHook
        GitBucketWebHook hook = new GitBucketWebHook();
        hook.doIndex(req);

        // make sure that onPost() never  called.
        verify(trigger, never()).onPost((GitBucketPushRequest) anyObject());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPushTrigger_NoPayload() throws Exception {
        // Repository URL
        String repo = j.createTmpDir().getAbsolutePath();

        // Setup FreeStyle Project
        FreeStyleProject fsp = j.createFreeStyleProject("NoPayload Project");

        // Setup Trigger
        GitBucketPushTrigger trigger = mock(GitBucketPushTrigger.class);
        fsp.addTrigger(trigger);

        // Setup SCM
        SCM scm = new GitSCM(repo);
        fsp.setScm(scm);

        // Setup WebHook request
        String payload = null;
        StaplerRequest req = mock(StaplerRequest.class);
        when(req.getParameter("payload")).thenReturn(payload);

        // Post WebHook
        GitBucketWebHook hook = new GitBucketWebHook();
        hook.doIndex(req);
    }

    @Test
    public void testPushTrigger_MultiSCM() throws Exception {
        // Repository URL
        String repo = j.createTmpDir().getAbsolutePath();

        // Setup FreeStyle Project
        FreeStyleProject fsp = j.createFreeStyleProject("MultiSCM Project");

        // Setup Trigger
        GitBucketPushTrigger trigger = mock(GitBucketPushTrigger.class);
        fsp.addTrigger(trigger);

        // Setup SCM
        SCM gitSCM = new GitSCM(repo);
        SCM nullSCM = new NullSCM();
        MultiSCM multiSCM = new MultiSCM(Arrays.asList(gitSCM, nullSCM));
        fsp.setScm(multiSCM);

        // Setup WebHook request
        String payload = createPayload(repo, "jenkins");
        StaplerRequest req = mock(StaplerRequest.class);
        when(req.getParameter("payload")).thenReturn(payload);

        // Post WebHook
        GitBucketWebHook hook = new GitBucketWebHook();
        hook.doIndex(req);

        verify(trigger, times(1)).onPost((GitBucketPushRequest) anyObject());
    }

    @Test
    public void testPushTrigger_NullSCM() throws Exception {
        // Repository URL
        String repo = j.createTmpDir().getAbsolutePath();

        // Setup FreeStyle Project
        FreeStyleProject fsp = j.createFreeStyleProject("NullSCM Project");

        // Setup Trigger
        GitBucketPushTrigger trigger = mock(GitBucketPushTrigger.class);
        fsp.addTrigger(trigger);

        // Setup SCM
        SCM nullSCM = new NullSCM();
        fsp.setScm(nullSCM);

        // Setup WebHook request
        String payload = createPayload(repo, "jenkins");
        StaplerRequest req = mock(StaplerRequest.class);
        when(req.getParameter("payload")).thenReturn(payload);

        // Post WebHook
        GitBucketWebHook hook = new GitBucketWebHook();
        hook.doIndex(req);

        verify(trigger, never()).onPost((GitBucketPushRequest) anyObject());
    }

    /**
     * {
     * "pusher":{"name":"jenkins",#email":"jenkins@jenkins-ci.org"},
     * "repojitory":{"url": "http://git.jenkins-ci.org/jenkins.git"} }
     */
    private String createPayload(String url, String pusherName) {
        JSONObject json = new JSONObject();

        JSONObject repository = new JSONObject();
        repository.put("url", url);
        json.put("repository", repository);

        JSONObject pusher = new JSONObject();
        pusher.put("name", pusherName);
        pusher.put("email", pusherName + "@jenkins-ci.org");
        json.put("pusher", pusher);

        return json.toString();
    }
}
