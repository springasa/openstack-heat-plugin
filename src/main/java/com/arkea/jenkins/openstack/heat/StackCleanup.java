package com.arkea.jenkins.openstack.heat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import com.arkea.jenkins.openstack.Constants;
import com.arkea.jenkins.openstack.client.OpenStack4jClient;
import com.arkea.jenkins.openstack.exception.utils.ExceptionUtils;
import com.arkea.jenkins.openstack.heat.configuration.ProjectOS;
import com.arkea.jenkins.openstack.heat.i18n.Messages;
import com.arkea.jenkins.openstack.log.ConsoleLogger;
import com.arkea.jenkins.openstack.operations.EnvVarsUtils;
import com.arkea.jenkins.openstack.operations.StackOperationsUtils;
import com.google.inject.Inject;

public class StackCleanup extends Publisher {
    private final Map<String, String> stackHotMap;
    private OpenStack4jClient clientOS;

    @DataBoundConstructor
    public StackCleanup(Map<String, String> stackHotMap) {
        this.stackHotMap = stackHotMap;
    }

    public StackCleanup(Map<String, String> stackHotMap, OpenStack4jClient clientOS) {
        /* Constructor for unittest, clientOS is mock */
        this.stackHotMap = stackHotMap;
        this.clientOS = clientOS;
    }

    /**
     * @return the bundle in JSON Format, it's easier to the render JavaScript
     */
    @JavaScriptMethod
    public JSONObject getStackHotMap() {
        return JSONObject.fromObject(this.stackHotMap);
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher,
                           BuildListener listener) throws IOException, InterruptedException {
        ConsoleLogger cLog = new ConsoleLogger(listener.getLogger(),
                "HOT Player", true);

        for (String key : stackHotMap.keySet()) {
            final String stackName = key;
            final String project = stackHotMap.get(key);
            try {
                // Variable in context
                EnvVarsUtils eVU = new EnvVarsUtils(build, listener, cLog);

                // Global configuration
                HOTPlayerSettings hPS = (HOTPlayerSettings) Jenkins.getInstance()
                        .getDescriptor(HOTPlayerSettings.class);

                // Project OpenStack to use
                ProjectOS projectOS = (ProjectOS) CollectionUtils.find(
                        hPS.getProjects(), new Predicate() {
                            public boolean evaluate(Object o) {
                                return project.equals(((ProjectOS) o).getProject());
                            }
                        });
                // Test if the project is found
                if (projectOS != null) {

                    // Client OpenStack inject during test or client failed
                    if (clientOS == null || !clientOS.isConnectionOK()) {
                        clientOS = new OpenStack4jClient(projectOS);
                    }

                    if (!StackOperationsUtils.deleteStack(
                            eVU.getVar(stackName), clientOS, cLog,
                            hPS.getTimersOS())) {
                        cLog.logError(Messages.delete_failed(project));
                        return false;
                    }
                } else {
                    cLog.logError(Messages.project_notFound(project));
                    return false;
                }

            } catch (Exception e) {
                cLog.logError(Messages.processing_failed(stackName)
                        + ExceptionUtils.getStackTrace(e));
                return false;
            }
        }

        return true;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Extension(ordinal = -9999)
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        private HOTPlayerSettings hotPlayerSettings;

        public DescriptorImpl() {
            super(StackCleanup.class);
        }

        @Inject
        public DescriptorImpl(HOTPlayerSettings hotPlayerSettings) {
            this.hotPlayerSettings = hotPlayerSettings;
        }

        @Override
        public String getDisplayName() {
            return "Delete HOT player stack when build done";
        }

        @Override
        public boolean isApplicable(Class clazz) {
            try {
                return hotPlayerSettings.checkData();
            } catch (hudson.model.Descriptor.FormException e) {
                return false;
            }
        }

        @Override
        public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            Map<String, String> stackHotMap = new HashMap<>();
            if (formData.containsKey(Constants.CLEANSTACK_INFO)) {
                Object info = formData.get(Constants.CLEANSTACK_INFO);
                if (info instanceof JSONObject) {
                    putToCleanStackMap(stackHotMap, formData.getJSONObject(Constants.CLEANSTACK_INFO));
                } else if (info instanceof JSONArray) {
                    JSONArray cleanStackInfos = formData.getJSONArray(Constants.CLEANSTACK_INFO);
                    for (Object cleanStackInfo : cleanStackInfos) {
                        putToCleanStackMap(stackHotMap, (JSONObject) cleanStackInfo);
                    }
                }
            }
            return new StackCleanup(stackHotMap);
        }

        private void putToCleanStackMap(Map<String, String> stackHotMap, Map<String, Object> cleanStackMap) {
            String stack = (String) cleanStackMap.get(Constants.STACKNAME);
            String project = (String) cleanStackMap.get(Constants.PROJECT);
            stackHotMap.put(stack, project);
        }
    }
}
