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
import hudson.Util;
import hudson.console.AnnotatedLargeText;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Hudson.MasterComputer;
import hudson.model.Item;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.StringParameterValue;
import hudson.triggers.SCMTrigger.SCMTriggerCause;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import hudson.util.SequentialExecutionQueue;
import hudson.util.StreamTaskListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.jelly.XMLOutput;
import org.jenkinsci.plugins.gitbucket.GitBucketPushRequest.Commit;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Triggers a build when we receive a GitBucket WebHook.
 *
 * @author sogabe
 */
public class GitBucketPushTrigger extends Trigger<AbstractProject<?, ?>> {

    @DataBoundConstructor
    public GitBucketPushTrigger() {
    }

    public void onPost(final GitBucketPushRequest req) {
        getDescriptor().queue.execute(new Runnable() {
            private boolean polling() {
                try {
                    StreamTaskListener listener = new StreamTaskListener(getLogFile());

                    try {
                        PrintStream logger = listener.getLogger();

                        long start = System.currentTimeMillis();
                        logger.println("Started on " + DateFormat.getDateTimeInstance().format(new Date()));
                        boolean result = job.poll(listener).hasChanges();
                        logger.println("Done. Took " + Util.getTimeSpanString(System.currentTimeMillis() - start));

                        if (result) {
                            logger.println("Changes found");
                        } else {
                            logger.println("No changes");
                        }

                        return result;
                    } catch (Error e) {
                        e.printStackTrace(listener.error("Failed to record SCM polling"));
                        LOGGER.log(Level.SEVERE, "Failed to record SCM polling", e);
                        throw e;
                    } catch (RuntimeException e) {
                        e.printStackTrace(listener.error("Failed to record SCM polling"));
                        LOGGER.log(Level.SEVERE, "Failed to record SCM polling", e);
                        throw e;
                    } finally {
                        listener.closeQuietly();
                    }
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Failed to record SCM polling", e);
                }

                return false;
            }

            public void run() {
                LOGGER.log(Level.INFO, "{0} triggered.", job.getName());
                if (polling()) {
                    String name = " #" + job.getNextBuildNumber();
                    GitBucketPushCause cause = createGitBucketPushCause(req);
                    ParametersAction action = createParametersAction(req);
                    if (job.scheduleBuild(0, cause, action)) {
                        LOGGER.log(Level.INFO, "SCM changes detected in {0}. Triggering {1}", new String[]{job.getName(), name});
                    } else {
                        LOGGER.log(Level.INFO, "SCM changes detected in {0}. Job is already in the queue.", job.getName());
                    }
                }
            }
            
            private GitBucketPushCause createGitBucketPushCause(GitBucketPushRequest req) {
                GitBucketPushCause cause;
                String triggeredByUser = req.getPusher().getName();
                try {
                    cause = new GitBucketPushCause(triggeredByUser, getLogFile());
                } catch (IOException ex) {
                    cause = new GitBucketPushCause(triggeredByUser);
                }
                return cause;
            }
            
            private ParametersAction createParametersAction(GitBucketPushRequest req) {
                ArrayList<ParameterValue> values = new ArrayList<ParameterValue>();
                
                // ${sha1}
                List<Commit> commits = req.getCommits();
                Commit lastCommit = commits.get(commits.size() - 1);
                values.add(new StringParameterValue("sha1", lastCommit.getId()));
                
                return new ParametersAction(values);
            }
        });
    }

    public static class GitBucketPushCause extends SCMTriggerCause {

        private final String pushedBy;

        public GitBucketPushCause(String pushedBy) {
            this.pushedBy = pushedBy;
        }

        public GitBucketPushCause(String pushedBy, File logFile) throws IOException {
            super(logFile);
            this.pushedBy = pushedBy;
        }

        public GitBucketPushCause(String pushedBy, String pollingLog) {
            super(pollingLog);
            this.pushedBy = pushedBy;
        }

        @Override
        public String getShortDescription() {
            if (pushedBy == null) {
                return "Started by GitBucket push";
            } else {
                return "Started by GitBucket push by " + pushedBy;
            }
        }
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        return Collections.singletonList(new GitBucketWebHookPollingAction());
    }
    
    public class GitBucketWebHookPollingAction implements Action {

        public AbstractProject<?, ?> getOwner() {
            return job;
        }

        public String getIconFileName() {
            return "/plugin/gitbucket/images/24x24/gitbucket-log.png";
        }

        public String getDisplayName() {
            return "GitBucket Hook Log";
        }

        public String getUrlName() {
            return "GitBucketPollLog";
        }

        public String getLog() throws IOException {
            return Util.loadFile(getLogFile());
        }

        public void writeLogTo(XMLOutput out) throws IOException {
            new AnnotatedLargeText<GitBucketWebHookPollingAction>(
                    getLogFile(), Charset.defaultCharset(), true, this).writeHtmlTo(0, out.asWriter());
        }
    }

    @Override
    public GitBucketPushTriggerDescriptor getDescriptor() {
        return (GitBucketPushTriggerDescriptor) super.getDescriptor();
    }

    public File getLogFile() {
        return new File(job.getRootDir(), "gitbucket-polling.log");
    }

    @Extension
    public static class GitBucketPushTriggerDescriptor extends TriggerDescriptor {

        private transient final SequentialExecutionQueue queue = new SequentialExecutionQueue(MasterComputer.threadPoolForRemoting);

        @Override
        public boolean isApplicable(Item item) {
            return item instanceof AbstractProject;
        }

        @Override
        public String getDisplayName() {
            return "Build when a change is pushed to GitBucket";
        }

        @Override
        public String getHelpFile() {
            return "/plugin/gitbucket/help/help-trigger.html";
        }

    }
    private static final Logger LOGGER = Logger.getLogger(GitBucketPushTrigger.class.getName());
}