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
        this.url = normalizeToEndWithSlash(new URL(url));
    }

    public URL getUrl() {
        return url;
    }

    @Override
    public URL getChangeSetLink(GitChangeSet changeSet) throws IOException {
        return new URL(url, url.getPath() + "commit/" + changeSet.getId().toString());
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
            String spec = "blob/" + path.getChangeSet().getId() + "/" + path.getPath();
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