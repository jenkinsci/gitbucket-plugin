package org.jenkinsci.plugins.gitbucket;

import hudson.Extension;
import hudson.Util;
import hudson.matrix.MatrixRun;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import java.util.Collection;
import java.util.Collections;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Project property
 * 
 * @author sogabe
 */
public class GitBucketProjectProperty extends JobProperty<AbstractProject<?, ?>> {

    private String url;
    
    private boolean linkEnabled;

    public String getUrl() {
        return url;
    }

    public boolean isLinkEnabled() {
        return linkEnabled;
    }

    @DataBoundConstructor
    public GitBucketProjectProperty(String url, boolean linkEnabled) {
        this.url = normalizeUrl(url);
        this.linkEnabled = linkEnabled;
    }
    
    private String normalizeUrl(String url) {
        String u = Util.fixEmptyAndTrim(url);
        if (u == null) {
            return null;
        }
        if (u.endsWith("/")) {
            return u;
        }
        return u + "/";
    }

    @Override
    public Collection<? extends Action> getJobActions(AbstractProject<?, ?> job) {
        if (url == null) {
            return Collections.EMPTY_LIST;
        }
        return Collections.singletonList(new GitBucketLinkAction(this));
    }
    
    public static GitBucketProjectProperty get(AbstractBuild<?, ?> build) {
        if (build == null) {
            return null;
        }
        Job<?, ?> job;
        if (build instanceof MatrixRun) {
            job = ((MatrixRun) build).getProject().getParent();
        } else {
            job = build.getProject();
        }
        return job.getProperty(GitBucketProjectProperty.class);
    }

    @Extension
    public static final class DescriptorImpl extends JobPropertyDescriptor {

        @Override
        public String getDisplayName() {
            return "GitBucket";
        }
    }
}
