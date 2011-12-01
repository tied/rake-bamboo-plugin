package au.id.wolfe.bamboo.ruby.rake;

import au.id.wolfe.bamboo.ruby.common.BaseRubyTask;
import au.id.wolfe.bamboo.ruby.rvm.RubyLocator;
import au.id.wolfe.bamboo.ruby.rvm.RubyRuntime;
import au.id.wolfe.bamboo.ruby.rvm.RvmLocatorService;
import au.id.wolfe.bamboo.ruby.rvm.RvmUtil;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.process.EnvironmentVariableAccessor;
import com.atlassian.bamboo.process.ExternalProcessBuilder;
import com.atlassian.bamboo.process.ProcessService;
import com.atlassian.bamboo.task.*;
import com.atlassian.bamboo.v2.build.agent.capability.CapabilityDefaultsHelper;
import com.atlassian.utils.process.ExternalProcess;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Bamboo task which interfaces with RVM and runs ruby make (rake).
 */
public class RakeTask extends BaseRubyTask implements TaskType {

    private static final String BUNDLE_COMMAND = "bundle";
    private static final String RAKE_COMMAND = "rake";

    public RakeTask(ProcessService processService, RvmLocatorService rvmLocatorService, EnvironmentVariableAccessor environmentVariableAccessor) {
        super(processService, rvmLocatorService, environmentVariableAccessor);
    }

    @Override
    protected List<String> buildCommandList(@NotNull TaskContext taskContext) {

        final ConfigurationMap config = taskContext.getConfigurationMap();
        final RubyLocator rubyLocator = getRubyLocator();

        final String rubyRuntimeName = config.get("ruby");
        Preconditions.checkArgument(rubyRuntimeName != null);

        final String targets = config.get("targets");
        Preconditions.checkArgument(targets != null);

        final String bundleExec = config.get("bundleexec");

        List<String> targetList = RvmUtil.splitRakeTargets(targets);

        final RubyRuntime rubyRuntime = rubyLocator.getRubyRuntime(rubyRuntimeName);

        List<String> commandsList = Lists.newLinkedList();

        commandsList.add(rubyRuntime.getRubyExecutablePath());

        if (bundleExec == null || bundleExec.equals("false")) {
            commandsList.add(rubyLocator.searchForRubyExecutable(rubyRuntimeName, RAKE_COMMAND));
        } else {
            commandsList.add(rubyLocator.searchForRubyExecutable(rubyRuntimeName, BUNDLE_COMMAND));
            commandsList.add("exec");
            commandsList.add(rubyLocator.searchForRubyExecutable(rubyRuntimeName, RAKE_COMMAND));
        }
        commandsList.addAll(targetList);

        return commandsList;
    }

    @Override
    protected Map<String, String> buildEnvironment(@NotNull TaskContext taskContext) {

        final ConfigurationMap config = taskContext.getConfigurationMap();

        String rubyRuntimeName = config.get("ruby");
        Preconditions.checkArgument(rubyRuntimeName != null);

        Map<String, String> currentEnvVars = environmentVariableAccessor.getEnvironment();

        return getRubyLocator().buildEnv(rubyRuntimeName, currentEnvVars);
    }

}