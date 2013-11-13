/*
 * The MIT License
 *
 * Copyright 2013 Seiji Sogabe
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.util.JavaIdentifierTransformer;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Represents for WebHook payload
 *
 * @author sogabe
 */
public class GitBucketPushRequest {

    private User pusher;

    private String ref;

    private List<Commit> commits;

    private Repository repository;

    public static GitBucketPushRequest create(String payload) {
        if (payload == null) {
            throw new IllegalArgumentException("payload should not be null");
        }
        return create(JSONObject.fromObject(payload));
    }

    public static GitBucketPushRequest create(JSONObject payload) {
        if (payload == null || payload.isNullObject()) {
            throw new IllegalArgumentException("payload should not be null");
        }

        JsonConfig config = createJsonConfig();
        return (GitBucketPushRequest) JSONObject.toBean(payload, config);
    }

    private static JsonConfig createJsonConfig() {
        JsonConfig config = new JsonConfig();
        config.setRootClass(GitBucketPushRequest.class);

        Map<String, Class> classMap = new HashMap<String, Class>();
        classMap.put("commits", Commit.class);
        classMap.put("added", String.class);
        classMap.put("removed", String.class);
        classMap.put("modified", String.class);
        config.setClassMap(classMap);

        config.setJavaIdentifierTransformer(new JavaIdentifierTransformer() {

            @Override
            public String transformToJavaIdentifier(String param) {
                if (param == null) {
                    return null;
                }
                if ("private".equals(param)) {
                    return "private_";
                }
                return param;
            }

        });

        return config;
    }

    public GitBucketPushRequest() {
    }

    public User getPusher() {
        return pusher;
    }

    public void setPusher(User pusher) {
        this.pusher = pusher;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public List<Commit> getCommits() {
        return commits;
    }

    public void setCommits(List<Commit> commits) {
        this.commits = commits;
    }

    public Repository getRepository() {
        return repository;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    public static class Repository {

        private String name;

        private String url;

        private String description;

        private Integer forks;

        private boolean private_;

        private User owner;

        public Repository() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Integer getForks() {
            return forks;
        }

        public void setForks(Integer forks) {
            this.forks = forks;
        }

        public boolean isPrivate_() {
            return private_;
        }

        public void setPrivate_(boolean private_) {
            this.private_ = private_;
        }

        public User getOwner() {
            return owner;
        }

        public void setOwner(User owner) {
            this.owner = owner;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
        }

    }

    public static class Commit {

        private String id;

        private String message;

        private String timestamp;

        private String url;

        private List<String> added;

        private List<String> removed;

        private List<String> modified;

        public Commit() {
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public List<String> getAdded() {
            return added;
        }

        public void setAdded(List<String> added) {
            this.added = added;
        }

        public List<String> getRemoved() {
            return removed;
        }

        public void setRemoved(List<String> removed) {
            this.removed = removed;
        }

        public List<String> getModified() {
            return modified;
        }

        public void setModified(List<String> modified) {
            this.modified = modified;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
        }

    }

    public static class User {

        private String name;

        private String email;

        public User() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
        }
    }
}
