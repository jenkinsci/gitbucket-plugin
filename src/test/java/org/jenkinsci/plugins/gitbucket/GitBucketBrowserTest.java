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