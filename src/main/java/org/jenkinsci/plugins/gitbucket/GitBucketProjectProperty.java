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
        this.url = GitBucketUtil.trimEndSlash(url);
        this.linkEnabled = linkEnabled;
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
