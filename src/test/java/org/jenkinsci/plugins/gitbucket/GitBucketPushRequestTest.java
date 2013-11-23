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

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import org.jenkinsci.plugins.gitbucket.GitBucketPushRequest.User;
import static org.junit.Assert.assertThat;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author sogabe
 */
public class GitBucketPushRequestTest {

    private static String json;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        InputStream is = null;
        try {
            is = GitBucketPushRequestTest.class.getClassLoader().getResourceAsStream("org/jenkinsci/plugins/gitbucket/WebHookPayload.json");
            json = IOUtils.toString(is, "UTF-8");
            LOGGER.log(Level.INFO, "payload: {0}", json);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreate_String_Null() {
        GitBucketPushRequest.create((String) null);
    }

    @Test()
    public void testCreate_String() {
        GitBucketPushRequest req = GitBucketPushRequest.create(json);
        assertThat(req, notNullValue());
        
        User pusher = req.getPusher();
        assertThat(pusher, notNullValue());
        assertThat(pusher.getName(), is("sogabe"));
        assertThat(pusher.getEmail(), is("sogabe@xxx.ddo.jp"));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreate_JSONObject_Null() {
        GitBucketPushRequest.create((JSONObject) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreate_JSONObject_NullObject() {
        GitBucketPushRequest.create(new JSONObject(true));
    }
    
    private static final Logger LOGGER = Logger.getLogger(GitBucketPushRequestTest.class.getName());
}
