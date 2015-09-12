package org.jenkinsci.plugins.gitbucket;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Run;
import hudson.scm.ChangeLogSet;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.Secret;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.kohsuke.stapler.DataBoundConstructor;

@SuppressWarnings("unchecked")
public class GitBucketIssueUpdater extends Recorder {

    private static final Pattern[] PATTERNS = new Pattern[]{
        Pattern.compile("fix(|s|ed)\\s+#?(\\d+)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("close(|s|d)\\s+#?(\\d+)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("resolve(|s|d)\\s+#?(\\d+)", Pattern.CASE_INSENSITIVE)
    };

    @DataBoundConstructor
    public GitBucketIssueUpdater() {
        super();
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
            BuildListener listener) throws InterruptedException, IOException {

        PrintStream logger = listener.getLogger();
        logger.println("GitBucketIssueUpdater#perform called");

        List<Integer> ids = extractIdsFromChangeLog(build);
        try {
            updateIssue(build, 3);
        } catch (URISyntaxException ex) {
            //
        }

        return true;
    }

    @Nonnull
    private List<Integer> extractIdsFromChangeLog(AbstractBuild<?, ?> build) {
        List<Integer> ids = new ArrayList<Integer>();
        for (ChangeLogSet.Entry entry : build.getChangeSet()) {
            for (Pattern pattern : PATTERNS) {
                Matcher matcher = pattern.matcher(entry.getMsg());
                while (matcher.find()) {
                    ids.add(Integer.valueOf(matcher.group(1)));
                }
            }
        }
        return ids;
    }

    private void updateIssue(AbstractBuild<?, ?> build, int id)
            throws IOException, URISyntaxException {

        GitBucketProjectProperty property
                = GitBucketProjectProperty.get((Run<?, ?>) build);

        String url = createBaseUrl(property.getUrl());
        String token = Secret.toString(property.getToken());
        if ("".equals(token)) {
            // TODO
        }

        RequestConfig config = RequestConfig.custom().
                setConnectTimeout(10 * 1000).
                setSocketTimeout(10 * 1000).
                setMaxRedirects(3).
                setRedirectsEnabled(true).
                build();

        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("Content-Type", "application/json; charset=UTF-8"));
        headers.add(new BasicHeader("Accept-Charset", "utf-8"));
        headers.add(new BasicHeader("Accept-Language", "ja, en;q=0.8"));
        headers.add(new BasicHeader("User-Agent", "Jenkins GitBucket Plugin"));
        headers.add(new BasicHeader("Authorization", "token " + token));

        HttpClient httpClient = null;
        try {
            httpClient = HttpClientBuilder.create()
                    .setDefaultRequestConfig(config)
                    .setDefaultHeaders(headers).
                    build();

            HttpPost post = new HttpPost(url + "/issues/" + id + "/comments");
            String comment = createIssueComment(build);
            JSONObject json = new JSONObject();
            json.accumulate("body", comment);
            post.setEntity(new StringEntity(json.toString(), StandardCharsets.UTF_8));

            HttpResponse response = httpClient.execute(post);
        } finally {
            HttpClientUtils.closeQuietly(httpClient);
        }
    }

    private String createBaseUrl(String uri) throws URISyntaxException {
        URI u = new URI(uri);
        URIBuilder builder = new URIBuilder();
        return builder.setScheme(u.getScheme()).
                setHost(u.getHost()).
                setPort(u.getPort()).
                setPath("/api/v3/repos" + u.getPath()).
                build().toString();
    }

    private String createIssueComment(AbstractBuild<?, ?> build) {
        Jenkins jenkins = Jenkins.getInstance();
        if (jenkins == null) {
            return "";
        }
        String rootUrl = jenkins.getRootUrl();
        StringBuilder b = new StringBuilder();
        b.append("Integrated to ")
                .append("![")
                .append(build.getResult())
                .append("]")
                .append("(")
                .append(rootUrl).append("images/16x16/").append(build.getBuildStatusUrl())
                .append(")")
                .append("[")
                .append(build.getParent().getDisplayName()).append(" No.").append(build.getId())
                .append("](")
                .append(rootUrl).append(build.getUrl())
                .append(")");
        return b.toString();
    }

    @Extension
    public static final class GitBucketIssueUpdaterDescriptor extends
            BuildStepDescriptor<Publisher> {

        public GitBucketIssueUpdaterDescriptor() {
            super(GitBucketIssueUpdater.class);
        }

        @Override
        public String getDisplayName() {
            return "GitBucket update issue";
        }

        @Override
        @SuppressWarnings("raw")
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }
}
