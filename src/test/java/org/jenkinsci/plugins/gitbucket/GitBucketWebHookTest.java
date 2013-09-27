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
        String payload = createPayload(repo);
        StaplerRequest req = mock(StaplerRequest.class);
        when(req.getParameter("payload")).thenReturn(payload);

        // Post WebHook
        GitBucketWebHook hook = new GitBucketWebHook();
        hook.doIndex(req);

        verify(trigger, times(1)).onPost();
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
        String payload = createPayload("No Match Repository");
        StaplerRequest req = mock(StaplerRequest.class);
        when(req.getParameter("payload")).thenReturn(payload);

        // Post WebHook
        GitBucketWebHook hook = new GitBucketWebHook();
        hook.doIndex(req);

        // make sure that onPost() never  called.
        verify(trigger, never()).onPost();
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
        String payload = createPayload(repo);
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
        String payload = createPayload(repo);
        StaplerRequest req = mock(StaplerRequest.class);
        when(req.getParameter("payload")).thenReturn(payload);

        // Post WebHook
        GitBucketWebHook hook = new GitBucketWebHook();
        hook.doIndex(req);

        // make sure that onPost() never  called.
        verify(trigger, never()).onPost();
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
        String payload = createPayload(repo);
        StaplerRequest req = mock(StaplerRequest.class);
        when(req.getParameter("payload")).thenReturn(payload);

        // Post WebHook
        GitBucketWebHook hook = new GitBucketWebHook();
        hook.doIndex(req);

        verify(trigger, times(1)).onPost();
    }
    
    private String createPayload(String url) {
        JSONObject repository = new JSONObject();
        repository.put("url", url);
        JSONObject json = new JSONObject();
        json.put("repository", repository);
        return json.toString();
    }
}
