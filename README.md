# Jenkins Test Management Plugin
This plugin updates Jira issues with basic build information and useful artifacts (titled values, user defined files, screenshoots, stack traces).

## How it works with Java

This plugin works in tandem with [Test Management Adapter] which is the Maven dependency providing data gathering possibilities for your test framework. This plugin is responsible for `jira-tm-report.xml` parsing, report generating in accordance with [Jira Text Formatting Notation] and its publishing in corresponding issue comments via REST API.

![Scheme](/images/readme_main_scheme.jpg)

## How it works with .NET

This plugin works in tandem with [Test Management Adapter for .NET] which provides data gathering possibilities for your test framework. This plugin is responsible for `jira-tm-report.xml` parsing, report generating in accordance with [Jira Text Formatting Notation] and its publishing in corresponding issue comments via REST API.

![Scheme](/images/readme_main_scheme_NET.jpg)

## Installing a plugin

At this time, Test Management plugin installation is possible only **manually**. Unfortunately, the plugin has not yet been placed into the Jenkins Update Center.

### From the web UI

1. Download `.hpi` archive or build it from source code.
1. Navigate to the `Manage Jenkins` > `Manage Plugins` page in the web UI.
1. Click on the `Advanced` tab.
1. Choose the `.hpi` file under the `Upload Plugin` section.
1. Upload the plugin file.

![Plugin Manager Upload](https://jenkins.io/doc/book/resources/managing/plugin-manager-upload.png)

Once a plugin file has been uploaded, the Jenkins master must be manually restarted in order for the changes to take effect.

### How to generate `.hpi` archive from source code:

1. Download source code test-management-jenkins-plugin;
2. Click right mouse button on pom.xml, "Maven" > "Generate Sources and Update Folders" (for Intellej IDEA) for Messages class auto-generating (it will be placed in work directory);
3. Run maven compile;
4. "Test-management.hpi" will be placed in target directory.

### On the master

Assuming a `.hpi` file has been explicitly downloaded by a systems administrator, the administrator can manually place the .hpi file in a specific location on the file system.

Copy the downloaded `.hpi` file into the `JENKINS_HOME/plugins` directory on the Jenkins master (for example, on Debian systems JENKINS_HOME is generally `/var/lib/jenkins`).

The master will need to be restarted before the plugin is loaded and made available in the Jenkins environment.

## Plugin Usage

You need to add `Jira Test Management Results Updater` as your job post-build action. 

![Post-build Action](/images/readme_file_00.jpg)

You should fill out _url_, _username_ and _password_ fields with valid data (you can test connection using **Test connection** button). 

![Test Connection](/images/readme_file_01.jpg)

If you want to add `labels` to your project updated issues or take advantage of `clean-up` feature, you 
need to set some advanced options. You will need to hit the **Advanced** button to see them.

![Advanced button](/images/readme_file_02.jpg)

After that you can specify label type (prefix + build date or number) or expiration time period for clean-up function.
By default this plugin will search result files automatically. But if you wish to set the exact place to read results use `Custom 'target' folder location` option.

![Additional options](/images/post-build-action-full.jpg)

## Report Example

The Test result report is placed as Jira comment. Only build and status fields are mandatory, the others are optional. 

![Report example](/images/readme_file_04.jpg)

[Test Management Adapter]: https://github.com/teo-rakan/test-management-adapter.git
[Test Management Adapter for .NET]: https://git.epam.com/Ivan_Zakhartchouk/test-management-nadapter
[Jira Text Formatting Notation]: https://jira.atlassian.com/secure/WikiRendererHelpAction.jspa?section=all

