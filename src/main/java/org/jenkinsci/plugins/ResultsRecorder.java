package org.jenkinsci.plugins;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.util.IssuesExecutor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

public class ResultsRecorder extends Recorder {
    private static final String HTTP_HTTPS_URL_REGEX = "^https?://.*";

    private final String jiraUrl;
    private final String username;
    private final String password;
    private final String dateCriteria;
    private final String deleteCriteria;
    private final boolean toDelete;

    @DataBoundConstructor
    public ResultsRecorder(String jiraUrl, String username, String password, String dateCriteria, String deleteCriteria, boolean toDelete) {
        this.jiraUrl = jiraUrl;
        this.username = username;
        this.password = password;
        this.dateCriteria = dateCriteria;
        this.deleteCriteria = deleteCriteria;
        this.toDelete = toDelete;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws IOException {
        PrintStream logger = listener.getLogger();
        logger.println(dateCriteria);
        logger.println(deleteCriteria);
        logger.println("--------------------------------------------------------");
        TestManagementService client = new TestManagementService(getJiraUrl(), getUsername(), getPassword(), build, logger);
        IssuesExecutor executor = new IssuesExecutor(client, logger);
        executor.execute(new File(build.getProject().getSomeWorkspace() + "/target/tm-testng.xml"), deleteCriteria, dateCriteria);
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

    public String getDateCriteria() {
        return dateCriteria;
    }

    public String getDeleteCriteria() {
        return deleteCriteria;
    }

    public boolean isToDelete() {
        return toDelete;
    }



    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        boolean toDelete;

        public ListBoxModel doFillDateCriteriaItems() {
            ListBoxModel items = new ListBoxModel();
            items.add("Year", "1");
            items.add("Month", "2");
            items.add("Week", "3");
            items.add("Day", "5");
            items.add("Hour", "10");
            items.add("Minute", "12");
            return items;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            JSONObject jsonObject = json.getJSONObject("toDelete");
            if (jsonObject != null && !jsonObject.isNullObject()) {
                toDelete = true;
            }
            else toDelete=false;
            save();
            return super.configure(req, json);
        }

        public DescriptorImpl() {
           load();
        }

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
