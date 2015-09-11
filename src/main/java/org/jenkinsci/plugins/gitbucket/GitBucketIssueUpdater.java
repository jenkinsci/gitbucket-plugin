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
import java.io.IOException;
import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.kohsuke.stapler.DataBoundConstructor;

@SuppressWarnings("unchecked")
public class GitBucketIssueUpdater extends Recorder {

    private final String token;

    @DataBoundConstructor
    public GitBucketIssueUpdater(String token) {
        super();
        this.token = token;
    }

    public String getToken() {
        return token;
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
        doPerform(build, logger);
        return true;
    }

    private void doPerform(AbstractBuild<?,?> build, PrintStream logger)
            throws InterruptedException, IOException {
        GitBucketProjectProperty property
                = GitBucketProjectProperty.get((Run<?, ?>) build);
        String url = property.getUrl();

        Pattern[] patterns = new Pattern[] {
            Pattern.compile("fix(|s|ed)\\s+#?(\\d+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("close(|s|d)\\s+#?(\\d+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("resolve(|s|d)\\s+#?(\\d+)", Pattern.CASE_INSENSITIVE),
        };

        for (ChangeLogSet.Entry entry : build.getChangeSet()) {
            for (Pattern pattern : patterns) {
                Matcher matcher = pattern.matcher(entry.getMsg());
                while (matcher.find()) {
                    int id = Integer.valueOf(matcher.group(1));
                    logger.println("[gitbucket] issue id: " + id);
                }
            }
        }
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
