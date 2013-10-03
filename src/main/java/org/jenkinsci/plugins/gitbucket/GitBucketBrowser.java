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
import hudson.model.Descriptor;
import hudson.plugins.git.GitChangeSet;
import hudson.plugins.git.GitChangeSet.Path;
import hudson.plugins.git.browser.GitRepositoryBrowser;
import hudson.scm.EditType;
import hudson.scm.RepositoryBrowser;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * GitBucket Browser URLs
 */
public class GitBucketBrowser extends GitRepositoryBrowser {

    private static final long serialVersionUID = 1L;

    private URL url;

    @DataBoundConstructor
    public GitBucketBrowser(String url) throws MalformedURLException {
        String normalizedUrl = GitBucketUtil.trimEndSlash(url);
        this.url = new URL(normalizedUrl);
    }
    
    public URL getUrl() {
        return url;
    }

    @Override
    public URL getChangeSetLink(GitChangeSet changeSet) throws IOException {
        return new URL(url, url.getPath() + "/commit/" + changeSet.getId().toString());
    }

    @Override
    public URL getDiffLink(Path path) throws IOException {
        if (path.getEditType() != EditType.EDIT || path.getSrc() == null || path.getDst() == null
                || path.getChangeSet().getParentCommit() == null) {
            return null;
        }
        return getDiffLinkRegardlessOfEditType(path);
    }

    private URL getDiffLinkRegardlessOfEditType(Path path) throws IOException {
        GitChangeSet changeSet = path.getChangeSet();
        List<String> affectedPaths = new ArrayList<String>(changeSet.getAffectedPaths());
        Collections.sort(affectedPaths);
        String pathAsString = path.getPath();
        int i = Collections.binarySearch(affectedPaths, pathAsString);
        assert i >= 0;
        return new URL(getChangeSetLink(changeSet), "#diff-" + String.valueOf(i));
    }

    @Override
    public URL getFileLink(Path path) throws IOException {
        if (path.getEditType().equals(EditType.DELETE)) {
            return getDiffLinkRegardlessOfEditType(path);
        } else {
            String spec = "/blob/" + path.getChangeSet().getId() + "/" + path.getPath();
            return new URL(url, url.getPath() + spec);
        }
    }

    @Extension
    public static class GitBucketBrowserDescriptor extends Descriptor<RepositoryBrowser<?>> {

        public String getDisplayName() {
            return "GitBucket";
        }

        @Override
        public GitBucketBrowser newInstance(StaplerRequest req, JSONObject jsonObject) throws FormException {
            return req.bindParameters(GitBucketBrowser.class, "GitBucket.");
        }
    }

}