package cn.mrdear.intellij.decompile.ui;

import cn.mrdear.intellij.decompile.OpenHelperWebSiteAction;
import cn.mrdear.intellij.decompile.util.ExternalToolsProcessListener;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.tools.Tool;
import com.intellij.tools.ToolManager;
import org.apache.commons.lang3.StringUtils;

import java.io.StringWriter;
import java.util.List;

/**
 * @author Quding Ding
 * @since 2020/3/7
 */
public class JadToolPanel extends AbstractToolPanel {

    public JadToolPanel(Project project) {
        super(project);
    }

    @Override
    protected AnAction internalToolAction() {
        return new OpenHelperWebSiteAction("http://www.kpdus.com/jad.html");
    }

    /**
     * 得到当前实例
     *
     * @param project 当前工程
     * @return 结果
     */
    public static JadToolPanel getInstance(Project project) {
        return ServiceManager.getService(project, JadToolPanel.class);
    }

    /**
     * 反编译为字节码
     */
    public void decompile(String path) {
        StringWriter writer = new StringWriter();

        List<Tool> tools = ToolManager.getInstance().getTools("External Tools");

        Tool jad = tools.stream()
            .filter(x -> x.getName().equalsIgnoreCase("jad"))
            .findFirst().orElse(null);

        if (null == jad) {
            this.setCode("\n no jad command found on External Tools, you can refer to " +
                "https://github.com/mrdear/class-decompile-intellij");
            return;
        }

        GeneralCommandLine commandLine = jad.createCommandLine((dataId) -> {
            if (CommonDataKeys.PROJECT.getName().equals(dataId)) {
                return project;
            }
            return null;
        });

        if (null == commandLine) {
            return;
        }

        commandLine.withParameters(StringUtils.split(SETTING.getJad()," "));
        commandLine.addParameter(path);

        try {
            ExternalToolsProcessListener listener = new ExternalToolsProcessListener(writer, this);
            OSProcessHandler handler = new OSProcessHandler(commandLine);
            handler.addProcessListener(listener);
            handler.startNotify();
        } catch (Exception e) {
            this.setCode("decompile fail " + e.getMessage());
        }
    }

}
