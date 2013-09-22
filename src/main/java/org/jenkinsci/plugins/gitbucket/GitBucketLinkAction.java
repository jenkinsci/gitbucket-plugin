package org.jenkinsci.plugins.gitbucket;

import hudson.model.Action;

/**
 * Add the GitBucket Icon/Link to the sidebar.
 * 
 * @author sogabe
 */
public class GitBucketLinkAction implements Action {

    private transient GitBucketProjectProperty property;

    public GitBucketLinkAction(GitBucketProjectProperty property) {
        this.property = property;
    }
    
    public String getIconFileName() {
        return "/plugin/gitbucket/images/24x24/gitbucket.png";
    }

    public String getDisplayName() {
        return "GitBucket";
    }

    public String getUrlName() {
        return property.getUrl();
    }
        
}
