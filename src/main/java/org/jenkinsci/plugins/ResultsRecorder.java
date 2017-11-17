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
import org.jenkinsci.plugins.util.Label;
import org.jenkinsci.plugins.util.LabelOption;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Calendar;


/**
 * ResultsRecorder is a basic class providing essential plugin functionality as
 * Jenkins post-build action. All actions with issues are done in perform method.
 * All Jenkins UI interactions are placed in Descriptor class implementation.
 *
 * @author Uladzimir Pryhazhanau
 */
public class ResultsRecorder extends Recorder {

    private final String jiraUrl;
    private final String username;
    private final String password;
    private final String dateCriteria;
    private final String deleteCriteria;
    private final boolean toDelete;
    private final boolean addLabel;
    private final String prefix;
    private final String addInfo;

    @DataBoundConstructor
    public ResultsRecorder(String jiraUrl,
                           String username,
                           String password,
                           String dateCriteria,
                           String deleteCriteria,
                           boolean toDelete,
                           boolean addLabel,
                           String prefix,
                           String addInfo) {
        this.jiraUrl = jiraUrl;
        this.username = username;
        this.password = password;
        this.dateCriteria = dateCriteria;
        this.deleteCriteria = deleteCriteria;
        this.toDelete = toDelete;
        this.addLabel = addLabel;
        this.prefix = prefix;
        this.addInfo = addInfo;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws IOException {
        PrintStream logger = listener.getLogger();
        String workspace = build.getProject().getSomeWorkspace().getRemote();
        int buildNumber = build.number;
        File xml = new File(workspace + "/target/tm-testng.xml");
        String formattedLabel = null;
        String deleteCriteria = null;
        String dateCriteria = null;
        TestManagementService service;

        // If the "add label" option is selected in "Advanced" section
        if (isAddLabel()) {
            Label label = new Label(prefix, LabelOption.valueOf(addInfo));
            formattedLabel = label.needDate() ? label.formatWithTodayDate() : label.formatWith(buildNumber);
        }

        // If the "clean-up" option is selected in "Advanced" section
        if (isToDelete()) {
            deleteCriteria = getDeleteCriteria();
            dateCriteria = getDateCriteria();
        }

        service = new TestManagementService(getJiraUrl(), getUsername(), getPassword(), workspace, buildNumber, logger);
        new IssuesExecutor(service, logger).execute(xml, deleteCriteria, dateCriteria, formattedLabel);

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

    public String getPrefix() {
        return prefix;
    }

    public String getAddInfo() {
        return addInfo;
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
            items.add(Messages.Date_Year(), String.valueOf(Calendar.YEAR));
            items.add(Messages.Date_Month(), String.valueOf(Calendar.MONTH));
            items.add(Messages.Date_Week(), String.valueOf(Calendar.WEEK_OF_YEAR));
            items.add(Messages.Date_Day(), String.valueOf(Calendar.DATE));
            items.add(Messages.Date_Hour(), String.valueOf(Calendar.HOUR_OF_DAY));
            items.add(Messages.Date_Minute(), String.valueOf(Calendar.MINUTE));
            return items;
        }

        public ListBoxModel doFillAddInfoItems() {
            ListBoxModel items = new ListBoxModel();
            items.add(Messages.Label_Build_Date(), LabelOption.BUILD_DATE.toString());
            items.add(Messages.Label_Build_Number(), LabelOption.BUILD_NUMBER.toString());
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
                    return FormValidation.ok(Messages.FormValidation_ConnectionSuccessful());
                case 500:
                    return FormValidation.warning(Messages.FormValidation_InternalError());
                case 401:
                    return FormValidation.warning(Messages.FormValidation_AuthorizationFailed());
                case 404:
                    return FormValidation.error(Messages.FormValidation_InvalidUrl());
                default:
                    return FormValidation.error(Messages.FormValidation_UnknownError() + status);
            }
        }

        public FormValidation doCheckPrefix(@QueryParameter String value) {
            return value.length() <= 15 ? FormValidation.ok() : FormValidation.error("Too many characters");
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
