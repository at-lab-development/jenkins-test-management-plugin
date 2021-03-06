package org.jenkinsci.plugins;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
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
import org.apache.commons.io.FileUtils;
import org.jenkinsci.plugins.api.TestManagementService;
import org.jenkinsci.plugins.util.IssuesExecutor;
import org.jenkinsci.plugins.util.Label;
import org.jenkinsci.plugins.util.LabelOption;
import org.kohsuke.stapler.AncestorInPath;
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
    private final boolean workspacePathEnabled;
    private final String workspacePath;
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
                           boolean workspacePathEnabled,
                           String workspacePath,
                           String dateCriteria,
                           String deleteCriteria,
                           boolean toDelete,
                           boolean addLabel,
                           String prefix,
                           String addInfo) {
        this.jiraUrl = jiraUrl;
        this.username = username;
        this.password = password;
        this.workspacePathEnabled = workspacePathEnabled;
        this.workspacePath = workspacePath;
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
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        PrintStream logger = listener.getLogger();
        String workspace = resolveWorkspacePath(build, listener);
        File xml = new File(workspace + Constants.TEST_RESULTS_FILE_PATH);

        int buildNumber = build.number;
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

        if (xml.exists()) {
            FileUtils.copyFile(xml, new File(workspace + Constants.TEST_RESULTS_COPY_FILE_PATH));
            logger.println("Test results were saved into : " + workspace + Constants.TEST_RESULTS_COPY_FILE_PATH);
            FileUtils.deleteQuietly(xml);
        }

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

    public boolean isWorkspacePathEnabled() {
        return workspacePathEnabled;
    }

    public String getWorkspacePath() {
        return workspacePath;
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

    public String resolveWorkspacePath(AbstractBuild<?, ?> build, BuildListener listener) throws IOException, InterruptedException {
        String initialWorkspace = build.getProject().getSomeWorkspace().getRemote();
        String workspace = resultsLocation.findTargetFolderWithResultFile(initialWorkspace);

        if (!isWorkspacePathEnabled()) {
            return workspace;
        }

        String workspaceParameter = getWorkspacePath();
        if (workspaceParameter == null) {
            return workspace;
        }

        if (workspaceParameter.startsWith("\"") && workspaceParameter.endsWith("\"")) {
            workspaceParameter = workspaceParameter.substring(1, workspaceParameter.length() - 1).trim();
        }

        final EnvVars env = build.getEnvironment(listener);
        workspace = env.expand(workspaceParameter);

        return workspace;
    }

    private static class resultsLocation {
        private static String targetFolderPath;

        private static void findTargetFolder(String directory) {
            File[] filesArray = new File(directory).listFiles();
            for (File file : filesArray) {
                if (file.isDirectory() && !file.isHidden()) {
                    findTargetFolder(file.getAbsolutePath());
                }
                if (file.getAbsolutePath().endsWith(Constants.TEST_RESULTS_FILE_PATH)) {
                    targetFolderPath = file.getParentFile().getParent();
                }
            }
        }

        private static String findTargetFolderWithResultFile(String workspace) {
            findTargetFolder(workspace);
            return targetFolderPath;
        }
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        private String jiraUrl;
        private String user;
        private String password;
        private String workspacePath;

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

        public FormValidation doCheckWorkspacePath(@QueryParameter String value, @QueryParameter boolean workspacePathEnabled, @AncestorInPath AbstractProject project) {
            workspacePath = value;
            if (!workspacePathEnabled) {
                return FormValidation.ok();
            }

            if (value.length() == 0) {
                return FormValidation.error(Messages.FormValidation_EmptyWorkspacePath());
            }

            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1).trim();
            }

            FilePath someWorkspace = project.getSomeWorkspace();
            if (value.contains(Constants.ENV_VARS_WORKSPACE)) {
                value = value.replace(Constants.ENV_VARS_WORKSPACE, someWorkspace.getRemote());
            }

            File file = new File(value);
            if (!file.exists()) {
                return FormValidation.error(Messages.FormValidation_SpecifiedFolderNotFound());
            }

            return FormValidation.ok();
        }
    }
}
