/*
 * The MIT License
 *
 * Copyright 2013 Seiji Sogabe.
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

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.hamcrest.Matchers.is;
import org.junit.Test;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.doThrow;

/**
 * Unit Test for {@link GitBucketWebHook.GitBucketWebHookCrumbExclusin}
 * 
 * @author Seiji Sogabe
 */
public class GitBucketWebHookCrumbExclusionTest {

    private GitBucketWebHook.GitBucketWebHookCrumbExclusion target;
    
    @Before
    public void setUp() throws Exception {
        target = new GitBucketWebHook.GitBucketWebHookCrumbExclusion();
    }
            
    @Test
    public void testProcessPathInfoNull() throws IOException, ServletException {
        String pathInfo = null;
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        
        when(req.getPathInfo()).thenReturn(pathInfo);
        
        boolean actual = target.process(req, res, chain);
        
        assertThat(actual, is(false));
    }

    @Test
    public void testProcessWrongPathInfo() throws IOException, ServletException {
        String pathInfo = "/gitbucket-wrongwebhook/";
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        
        when(req.getPathInfo()).thenReturn(pathInfo);
        
        boolean actual = target.process(req, res, chain);
        
        assertThat(actual, is(false));
    }

    @Test
    public void testProcessPathInfo() throws IOException, ServletException {
        String pathInfo = '/' + GitBucketWebHook.WEBHOOK_URL + '/';
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        
        when(req.getPathInfo()).thenReturn(pathInfo);
                
        boolean actual = target.process(req, res, chain);
        
        assertThat(actual, is(true));
        verify(chain, times(1)).doFilter(req, res);
    }

    @Test(expected = IOException.class)
    public void testProcessThorowIOException() throws IOException, ServletException {
        String pathInfo = '/' + GitBucketWebHook.WEBHOOK_URL + '/';
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        
        when(req.getPathInfo()).thenReturn(pathInfo);
        doThrow(new IOException()).when(chain).doFilter(req, res);
        
        target.process(req, res, chain);
    }

    @Test(expected = ServletException.class)
    public void testProcessThorowServletException() throws IOException, ServletException {
        String pathInfo = '/' + GitBucketWebHook.WEBHOOK_URL + '/';
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        
        when(req.getPathInfo()).thenReturn(pathInfo);
        doThrow(new ServletException()).when(chain).doFilter(req, res);
        
        target.process(req, res, chain);
    }
    
    
}
