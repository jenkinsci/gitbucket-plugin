package org.jenkinsci.plugins.gitbucket;

import hudson.plugins.git.GitChangeSet;
import hudson.plugins.git.GitChangeSet.Path;
import hudson.scm.EditType;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

/**
 * Test for {@link GitBucketBrowser} class.
 *
 * @author sogabe
 */
public class GitBucketBrowserTest {

    private static final String GITBUCKET_URL = "http://localhost/gitbucket/sogabe/gitbucket-plugin/";

    private GitBucketBrowser target;

    @Before
    public void setUp() throws Exception {
        target = new GitBucketBrowser(GITBUCKET_URL);
    }

    @Test
    public void testGetChangeSetLink() throws IOException {
        String id = "1";
        GitChangeSet mockGitChangeSet = mock(GitChangeSet.class);
        when(mockGitChangeSet.getId()).thenReturn(id);

        URL actual = target.getChangeSetLink(mockGitChangeSet);

        assertThat(actual.toString(), is(GITBUCKET_URL + "commit/" + id));
    }

    @Test
    public void testGetDiffLink_NotEDIT() throws IOException {
        EditType editType = EditType.ADD;
        Path mockPath = mock(Path.class);
        when(mockPath.getEditType()).thenReturn(editType);

        URL actual = target.getDiffLink(mockPath);

        assertThat(actual, nullValue());
    }

    @Test
    public void testGetDiffLink_NoSrc() throws IOException {
        Path mockPath = mock(Path.class);
        when(mockPath.getSrc()).thenReturn(null);

        URL actual = target.getDiffLink(mockPath);

        assertThat(actual, nullValue());
    }

    @Test
    public void testGetDiffLink_NoDst() throws IOException {
        Path mockPath = mock(Path.class);
        when(mockPath.getDst()).thenReturn(null);

        URL actual = target.getDiffLink(mockPath);

        assertThat(actual, nullValue());
    }

    @Test
    public void testGetDiffLink_NoParentCommit() throws IOException {
        Path mockPath = mock(Path.class);
        GitChangeSet mockGitChangeSet = mock(GitChangeSet.class);
        when(mockPath.getChangeSet()).thenReturn(mockGitChangeSet);
        when(mockGitChangeSet.getParentCommit()).thenReturn(null);

        URL actual = target.getDiffLink(mockPath);

        assertThat(actual, nullValue());
    }

    @Test
    public void testGetDiffLink() throws IOException {
        List<String> affectedPaths = Arrays.asList(
                "src/main/java/org/jenkinsci/plugins/gitbucket/GitBrowser.java", 
                "pom.xml",
                "README.md");
        String id = "1";
        
        Path mockPath = mock(Path.class);
        when(mockPath.getEditType()).thenReturn(EditType.EDIT);
        when(mockPath.getSrc()).thenReturn("pom.xml");
        when(mockPath.getDst()).thenReturn("pom.xml");
        when(mockPath.getPath()).thenReturn("pom.xml");
        
        GitChangeSet mockGitChangeSet = mock(GitChangeSet.class);
        when(mockPath.getChangeSet()).thenReturn(mockGitChangeSet);
        when(mockGitChangeSet.getAffectedPaths()).thenReturn(affectedPaths);
        when(mockGitChangeSet.getId()).thenReturn(id);        
        when(mockGitChangeSet.getParentCommit()).thenReturn("parent");        
        
        URL actual = target.getDiffLink(mockPath);

        assertThat(actual.toString(), is(GITBUCKET_URL + "commit/1#diff-1"));
    }
    
    @Test
    public void testGetFileLink_NotDelete() throws IOException {
        String id = "1";
        
        Path mockPath = mock(Path.class);
        when(mockPath.getEditType()).thenReturn(EditType.ADD);
        when(mockPath.getPath()).thenReturn("pom.xml");
        
        GitChangeSet mockGitChangeSet = mock(GitChangeSet.class);
        when(mockPath.getChangeSet()).thenReturn(mockGitChangeSet);
        when(mockGitChangeSet.getId()).thenReturn(id);        
        
        URL actual = target.getFileLink(mockPath);
        
        assertThat(actual.toString(), is(GITBUCKET_URL + "blob/" + id + "/pom.xml"));
    }

    @Test
    public void testGetFileLink_Delete() throws IOException {
        List<String> affectedPaths = Arrays.asList(
                "src/main/java/org/jenkinsci/plugins/gitbucket/GitBrowser.java", 
                "pom.xml",
                "README.md");
        String id = "1";
        
        Path mockPath = mock(Path.class);
        when(mockPath.getEditType()).thenReturn(EditType.DELETE);
        when(mockPath.getPath()).thenReturn("pom.xml");
        
        GitChangeSet mockGitChangeSet = mock(GitChangeSet.class);
        when(mockPath.getChangeSet()).thenReturn(mockGitChangeSet);
        when(mockGitChangeSet.getAffectedPaths()).thenReturn(affectedPaths);
        when(mockGitChangeSet.getId()).thenReturn(id);        
        
        URL actual = target.getFileLink(mockPath);

        assertThat(actual.toString(), is(GITBUCKET_URL + "commit/1#diff-1"));

    }
}