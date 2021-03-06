package org.joget.apps.app.lib;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.dao.FormDataDao;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.LogUtil;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.WorkflowProcessLink;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.util.WorkflowUtil;
import org.springframework.context.ApplicationContext;

public class FormHashVariable extends DefaultHashVariablePlugin {
    Map<String, FormRow> formDataCache = new HashMap<String, FormRow>();

    @Override
    public String processHashVariable(String variableKey) {
        String primaryKey = "";
        if (variableKey.contains("[") && variableKey.contains("]")) {
            primaryKey = variableKey.substring(variableKey.indexOf("[") + 1, variableKey.indexOf("]"));
            variableKey = variableKey.substring(0, variableKey.indexOf("["));
        }
        
        //get from request parameter if exist
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        if (primaryKey.isEmpty() && request != null && request.getParameter("id") != null) {
            primaryKey = request.getParameter("id");
        }
        
        String temp[] = variableKey.split("\\.");
        
        String tableName = temp[0];
        String columnName = temp[1];
        
        WorkflowAssignment wfAssignment = (WorkflowAssignment) this.getProperty("workflowAssignment");
        if (!primaryKey.isEmpty() || wfAssignment != null) {
            try {
                if (tableName != null && tableName.length() != 0) {
                    ApplicationContext appContext = AppUtil.getApplicationContext();
                    FormDataDao formDataDao = (FormDataDao) appContext.getBean("formDataDao");

                    if (primaryKey.isEmpty() && wfAssignment != null) {

                        WorkflowManager workflowManager = (WorkflowManager) appContext.getBean("workflowManager");
                        WorkflowProcessLink link = workflowManager.getWorkflowProcessLink(wfAssignment.getProcessId());

                        if (link != null) {
                            primaryKey = link.getOriginProcessId();
                        } else if (primaryKey.isEmpty()) {
                            primaryKey = wfAssignment.getProcessId();
                        }
                    }

                    String cacheKey = tableName + "##" + primaryKey;
                    FormRow row = formDataCache.get(cacheKey);
                    if (row == null) {        
                        row = formDataDao.loadByTableNameAndColumnName(tableName, columnName, primaryKey);
                        formDataCache.put(cacheKey, row);
                    }

                    if (row != null && row.getCustomProperties() != null) {
                        Object val = row.getCustomProperties().get(columnName);
                        if (val != null) {
                            return val.toString();
                        } else {
                            LogUtil.info(FormHashVariable.class.getName(), "#form." + variableKey + "# is NULL");
                            return "";
                        }
                    }
                }
            } catch (Exception ex) {}
        }
        return null;
    }

    public String getName() {
        return "Form Data Hash Variable";
    }

    public String getPrefix() {
        return "form";
    }

    public String getVersion() {
        return "3.0.0";
    }

    public String getDescription() {
        return "";
    }

    public String getLabel() {
        return "Form Data Hash Variable";
    }

    public String getClassName() {
       return this.getClass().getName();
    }

    public String getPropertyOptions() {
        return "";
    }
}
