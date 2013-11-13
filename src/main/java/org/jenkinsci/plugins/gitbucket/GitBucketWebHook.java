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
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.UnprotectedRootAction;
import hudson.plugins.git.GitSCM;
import hudson.scm.SCM;
import hudson.security.ACL;
import hudson.security.csrf.CrumbExclusion;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    public static final String WEBHOOK_URL = "gitbucket-webhook";

    public String getIconFileName() {
        return null;
    }

    public String getDisplayName() {
        return null;
    }

    public String getUrlName() {
        return WEBHOOK_URL;
    }

    @RequirePOST
    public void doIndex(StaplerRequest req) {
        LOGGER.log(Level.FINE, "WebHook called.");

        String payload = req.getParameter("payload");
        if (payload == null) {
            throw new IllegalArgumentException(
                    "Not intended to be browsed interactively (must specify payload parameter)");
        }

        processPayload(payload);
    }

    private void processPayload(String payload) {
        JSONObject json = JSONObject.fromObject(payload);
        LOGGER.log(Level.FINE, "payload: {0}", json.toString(4));
        LOGGER.log(Level.FINE, "payload: {0}", json.toString());

        GitBucketPushRequest req = GitBucketPushRequest.create(json);

        String repositoryUrl = req.getRepository().getUrl();
        if (repositoryUrl == null) {
            LOGGER.log(Level.WARNING, "No repository url found.");
            return;
        }

        Authentication old = SecurityContextHolder.getContext().getAuthentication();
        SecurityContextHolder.getContext().setAuthentication(ACL.SYSTEM);
        try {
            for (AbstractProject<?, ?> job : Jenkins.getInstance().getAllItems(AbstractProject.class)) {
                GitBucketPushTrigger trigger = job.getTrigger(GitBucketPushTrigger.class);
                if (trigger == null) {
                    continue;
                }
                if (RepositoryUrlCollector.collect(job).contains(repositoryUrl.toLowerCase())) {
                    trigger.onPost(req);
                }
            }
        } finally {
            SecurityContextHolder.getContext().setAuthentication(old);
        }
    }

    private String getPusherName(JSONObject payload) {
        JSONObject pusher = payload.getJSONObject("pusher");
        if (pusher.isNullObject()) {
            return null;
        }
        String name = (String) pusher.get("name");
        return Util.fixEmptyAndTrim(name);
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

    @Extension
    public static class GitBucketWebHookCrumbExclusion extends CrumbExclusion {

        @Override
        public boolean process(HttpServletRequest req, HttpServletResponse resp, FilterChain chain) throws IOException, ServletException {
            String pathInfo = req.getPathInfo();
            if (pathInfo != null && pathInfo.equals(getExclusionPath())) {
                chain.doFilter(req, resp);
                return true;
            }
            return false;
        }

        private String getExclusionPath() {
            return '/' + WEBHOOK_URL + '/';
        }
    }

    private static final Logger LOGGER = Logger.getLogger(GitBucketWebHook.class.getName());
}
