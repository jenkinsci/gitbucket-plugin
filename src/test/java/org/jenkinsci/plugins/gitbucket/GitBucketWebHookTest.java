package org.jenkinsci.plugins.gitbucket;

import hudson.model.FreeStyleProject;
import hudson.plugins.git.GitSCM;
import hudson.scm.SCM;
import java.util.Collections;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.multiplescms.MultiSCM;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;
import org.kohsuke.stapler.StaplerRequest;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;

/**
 * Test for {@link GitBucketWebHook} class.
 *
 * @author sogabe
 */
public class GitBucketWebHookTest extends HudsonTestCase {

    @Test
    public void testPushTrigger_GitSCM() throws Exception {
        // Repository URL
        String repo = createTmpDir().getAbsolutePath();

        // Setup FreeStyle Project
        FreeStyleProject fsp = createFreeStyleProject("GitSCM Project");

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
    public void testPushTrigger_NotMatchRepo() throws Exception {
        // Repository URL
        String repo = createTmpDir().getAbsolutePath();

        // Setup FreeStyle Project
        FreeStyleProject fsp = createFreeStyleProject("GitSCM Project");

        // Setup Trigger
        GitBucketPushTrigger trigger = mock(GitBucketPushTrigger.class);
        fsp.addTrigger(trigger);

        // Setup SCM
        SCM scm = new GitSCM(repo);
        fsp.setScm(scm);

        // Setup WebHook request
        String payload = createPayload("Not Match Repository");
        StaplerRequest req = mock(StaplerRequest.class);
        when(req.getParameter("payload")).thenReturn(payload);

        // Post WebHook
        GitBucketWebHook hook = new GitBucketWebHook();
        hook.doIndex(req);

        // make sure that onPost() never  called.
        verify(trigger, never()).onPost();
    }
    
    @Test
    public void testPushTrigger_NoPayload() throws Exception {
        // Repository URL
        String repo = createTmpDir().getAbsolutePath();

        // Setup FreeStyle Project
        FreeStyleProject fsp = createFreeStyleProject("NoPayload Project");

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
        try {
            hook.doIndex(req);
            fail();
        } catch (IllegalArgumentException e) {
            // OK
        }
    }

    @Test
    public void testPushTrigger_MultiSCM() throws Exception {
        // Repository URL
        String repo = createTmpDir().getAbsolutePath();

        // Setup FreeStyle Project
        FreeStyleProject fsp = createFreeStyleProject("MultiSCM Project");

        // Setup Trigger
        GitBucketPushTrigger trigger = mock(GitBucketPushTrigger.class);
        fsp.addTrigger(trigger);

        // Setup SCM
        SCM scm = new GitSCM(repo);
        MultiSCM multiSCM = new MultiSCM(Collections.singletonList(scm));
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
