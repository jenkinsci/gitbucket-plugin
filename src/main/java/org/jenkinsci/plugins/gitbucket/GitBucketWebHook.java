package org.jenkinsci.plugins.gitbucket;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.UnprotectedRootAction;
import hudson.plugins.git.GitSCM;
import hudson.scm.SCM;
import hudson.security.ACL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContextHolder;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.jenkinsci.plugins.multiplescms.MultiSCM;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.interceptor.RequirePOST;

/**
 * Receives GitBucket WebHook.
 *
 * @author sogabe
 */
@Extension
public class GitBucketWebHook implements UnprotectedRootAction {

    public String getIconFileName() {
        return null;
    }

    public String getDisplayName() {
        return null;
    }

    public String getUrlName() {
        return "gitbucket-webhook";
    }

    @RequirePOST
    public void doIndex(StaplerRequest req) {
        LOGGER.log(Level.FINE, "WebHook called.");

        String payload = req.getParameter("payload");
        if (payload == null) {
            throw new IllegalArgumentException(
                    "Not intended to be browsed interactively (must specify payload parameter)");
        }

        LOGGER.log(Level.FINE, "payload: {0}", payload);
        processPayload(payload);
    }

    private void processPayload(String payload) {
        JSONObject json = JSONObject.fromObject(payload);
        String repositoryUrl = json.getJSONObject("repository").getString("url").trim().toLowerCase();

        Authentication old = SecurityContextHolder.getContext().getAuthentication();
        SecurityContextHolder.getContext().setAuthentication(ACL.SYSTEM);
        try {
            for (AbstractProject<?, ?> job : Jenkins.getInstance().getAllItems(AbstractProject.class)) {
                GitBucketPushTrigger trigger = job.getTrigger(GitBucketPushTrigger.class);
                if (trigger == null) {
                    continue;
                }
                if (RepositoryUrlCollector.collect(job).contains(repositoryUrl)) {
                    // pusher not supported yet.
                    // TODO
                    trigger.onPost();
                }
            }
        } finally {
            SecurityContextHolder.getContext().setAuthentication(old);
        }
    }

    private static class RepositoryUrlCollector {

        public static List<String> collect(AbstractProject<?, ?> job) {
            List<String> urls = new ArrayList<String>();
            SCM scm = job.getScm();
            if (scm instanceof GitSCM) {
                urls.addAll(collect((GitSCM) scm));
            } else if (Jenkins.getInstance().getPlugin("multiple-scms") != null && scm instanceof MultiSCM) {
                MultiSCM multiSCM = (MultiSCM) scm;
                List<SCM> scms = multiSCM.getConfiguredSCMs();
                for (SCM s : scms) {
                    if (s instanceof GitSCM) {
                        urls.addAll(collect((GitSCM) s));
                    }
                }
            }
            return urls;
        }

        private static List<String> collect(GitSCM scm) {
            List<String> urls = new ArrayList<String>();
            for (RemoteConfig config : scm.getRepositories()) {
                for (URIish uri : config.getURIs()) {
                    String u = uri.toString();
                    urls.add(u.trim().toLowerCase());
                }
            }
            return urls;
        }
    }

    private static final Logger LOGGER = Logger.getLogger(GitBucketWebHook.class.getName());
}
