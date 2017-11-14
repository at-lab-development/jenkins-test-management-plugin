package org.jenkinsci.plugins;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import org.jenkinsci.plugins.api.TestManagementService;
import org.jenkinsci.plugins.util.IssuesExecutor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

public class ResultsRecorder extends Recorder {

    private final String jiraUrl;
    private final String username;
    private final String password;
    private final String dateCriteria;
    private final String deleteCriteria;
    private final boolean toDelete;
    private final boolean addLabel;

    @DataBoundConstructor
    public ResultsRecorder(String jiraUrl,
                           String username,
                           String password,
                           String dateCriteria,
                           String deleteCriteria,
                           boolean toDelete,
                           boolean addLabel) {
        this.jiraUrl = jiraUrl;
        this.username = username;
        this.password = password;
        this.dateCriteria = dateCriteria;
        this.deleteCriteria = deleteCriteria;
        this.toDelete = toDelete;
        this.addLabel = addLabel;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws IOException {
        TestManagementService service;
        PrintStream logger = listener.getLogger();
        String workspace = build.getProject().getSomeWorkspace().getRemote();
        File xml = new File(workspace + "/target/tm-testng.xml");

        service = new TestManagementService(getJiraUrl(), getUsername(), getPassword(), workspace, build.number, logger);
        new IssuesExecutor(service, logger).execute(xml, deleteCriteria, dateCriteria, isAddLabel());
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

    public boolean isAddLabel() {
        return addLabel;
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
        private String jiraUrl;
        private String user;
        private String password;

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


        public FormValidation doTestConnection() {
            int status = TestManagementService.checkConnection(jiraUrl, user, password);
            switch (status) {
                case 200:
                    return FormValidation.ok("Success");
                case 500:
                    return FormValidation.warning("Internal Server Error, check credentials");
                case 401:
                    return FormValidation.warning("Authorization failed");
                case 404:
                    return FormValidation.error("Not found, check URL");
                case 0:
                    return FormValidation.error("Critical error");
                default:
                    return FormValidation.error("Unknown error, status code: " + status);
            }
        }

        public FormValidation doCheckJiraUrl(@QueryParameter String value) {
            jiraUrl = value;
            return FormValidation.ok();
        }

        public FormValidation doCheckUsername(@QueryParameter String value) {
            user = value;
            return (value.length() == 0)
                    ? FormValidation.error(Messages.FormValidation_EmptyUsername())
                    : FormValidation.ok();
        }

        public FormValidation doCheckPassword(@QueryParameter String value) {
            password = value;
            return (value.length() == 0)
                    ? FormValidation.error(Messages.FormValidation_EmptyPassword())
                    : FormValidation.ok();
        }
    }
}
