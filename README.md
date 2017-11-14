# Jenkins Test Management Plugin
This plugin updates Jira issues with basic build information and useful artifacts (titled values, user defined files, screenshoots, stack traces).

## How it works

The key thing is that this plugin are working in tandem with [Test Management Adapter] which is the `Maven dependency` provading data gathering posibilities for your test framework. This plugin is responsible for `tm-testng.xml` parsing, report generating in accordance with Jira Text Formatting Notation and its publishing in corresponding issue comments via REST API.

![Scheme](https://github.com/teo-rakan/test-management-jenkins-plugin/blob/master/images/readme_scheme.jpg)

## Installing a plugin

At this time, Test Management plugin installation is possible only **manually**. Plugin is not placed yet into the Jenkins Update Center.

### From the web UI

1. Download `.hpi` archive or build it from source code.
1. Navigate to the `Manage Jenkins` > `Manage Plugins` page in the web UI.
1. Click on the `Advanced` tab.
1. Choose the `.hpi` file under the `Upload Plugin` section.
1. Upload the plugin file.

![Plugin Manager Upload](https://jenkins.io/doc/book/resources/managing/plugin-manager-upload.png)

Once a plugin file has been uploaded, the Jenkins master must be manually restarted in order for the changes to take effect.

## Plugin Using

You need to add `Jira Test Management Results Updater` as your job `post-build action`. You should fill 
out `url` and `credentials` fields with valid data (you can test connection using **Test connection** button). 

![Advanced Options](https://github.com/teo-rakan/test-management-jenkins-plugin/blob/master/images/readme_file_01.jpg)

If you want to add `labels` to your project updated issues or take advantage of `clean-up` function, you 
need to set some **advanced** options.

![Advanced Options](https://github.com/teo-rakan/test-management-jenkins-plugin/blob/master/images/readme_file_02.jpg)

[Test Management Adapter]: https://github.com/teo-rakan/test-management-adapter.git

