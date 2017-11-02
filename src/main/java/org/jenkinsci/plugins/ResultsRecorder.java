package org.jenkinsci.plugins;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import org.jenkinsci.plugins.util.IssuesExecutor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

public class ResultsRecorder extends Recorder {
    private static final String HTTP_HTTPS_URL_REGEX = "^https?://.*";

    private final String jiraUrl;
    private final String username;
    private final String password;

    @DataBoundConstructor
    public ResultsRecorder(String jiraUrl, String username, String password) {
        this.jiraUrl = jiraUrl;
        this.username = username;
        this.password = password;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws IOException {
        PrintStream logger = listener.getLogger();
        logger.println("--------------------------------------------------------");
        TestManagementService client = new TestManagementService(getJiraUrl(), getUsername(), getPassword(), build);
        IssuesExecutor executor = new IssuesExecutor(client, logger);
        executor.execute(new File(build.getProject().getSomeWorkspace() + "/target/tm-testng.xml"));
        return true;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    public String getJiraUrl() {
        return jiraUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.ResultsRecorder_DisplayName();
        }

        public FormValidation doCheckJiraUrl(@QueryParameter String value) {
            int status = new TestManagementService(value).checkConnection();
            if (status >= 400) return FormValidation.error("No connection, check your url");
            switch (status) {
                case 200 : return FormValidation.ok("Connected");
                case 0 : return FormValidation.error("Unknown error");
                default: return FormValidation.error("Unknown error, status code: " + status);
            }
        }

        public FormValidation doCheckUsername(@QueryParameter String value) {
            if (value.length() == 0) {
                return FormValidation.error(Messages.FormValidation_EmptyUsername());
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckPassword(@QueryParameter String value) {
            if (value.length() == 0) {
                return FormValidation.error(Messages.FormValidation_EmptyPassword());
            }
            return FormValidation.ok();
        }
    }
}
