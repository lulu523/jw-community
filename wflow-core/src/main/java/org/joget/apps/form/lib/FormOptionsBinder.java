package org.joget.apps.form.lib;

import java.util.Map;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.dao.FormDataDao;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormBinder;
import org.joget.apps.form.model.FormBuilderEditable;
import org.joget.apps.form.model.FormLoadOptionsBinder;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FormUtil;
import org.joget.plugin.base.PluginProperty;

/**
 * Form load binder that loads the data rows of a form.
 */
public class FormOptionsBinder extends FormBinder implements FormLoadOptionsBinder, FormBuilderEditable {

    @Override
    public String getName() {
        return "DefaultFormOptionsBinder";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Default Form Options Binder";
    }

    @Override
    public PluginProperty[] getPluginProperties() {
        return null;
    }

    @Override
    public Object execute(Map properties) {
        return null;
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getLabel() {
        return "Default Form Options Binder";
    }

    @Override
    public String getPropertyOptions() {
        String formDefField = null;
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        if (appDef != null) {
            String formJsonUrl = "[CONTEXT_PATH]/web/json/console/app/" + appDef.getId() + "/" + appDef.getVersion() + "/forms/options";
            formDefField = "{name:'formDefId',label:'@@form.defaultformoptionbinder.formId@@',type:'selectbox',options_ajax:'" + formJsonUrl + "',required : 'True'}";
        } else {
            formDefField = "{name:'formDefId',label:'@@form.defaultformoptionbinder.formId@@',type:'textfield',required : 'True'}";
        }
        Object[] arguments = new Object[]{formDefField};
        String json = AppUtil.readPluginResource(getClass().getName(), "/properties/form/formOptionsBinder.json", arguments, true, "message/form/DefaultFormOptionsBinder");
        return json;
    }

    @Override
    public FormRowSet load(Element element, String primaryKey) {
        FormRowSet results = new FormRowSet();
        results.setMultiRow(true);

        // get form
        String formDefId = (String) getProperty("formDefId");
        Form form = getSelectedForm(formDefId);
        if (form != null) {

            String condition = null;
            String extraCondition = (String) getProperty("extraCondition");
            if (extraCondition != null && !extraCondition.trim().isEmpty()) {
                condition = " WHERE " + extraCondition;
            }

            // get form data
            FormDataDao formDataDao = (FormDataDao) AppUtil.getApplicationContext().getBean("formDataDao");
            results = formDataDao.find(form, condition, null, null, null, null, null);

            if (results != null) {
                String labelColumn = (String) getProperty("labelColumn");

                // loop thru results to set value and label
                for (FormRow row : results) {
                    String id = row.getProperty(FormUtil.PROPERTY_ID);
                    String label = row.getProperty(labelColumn);
                    if (id != null && !id.isEmpty() && label != null && !label.isEmpty()) {
                        row.setProperty(FormUtil.PROPERTY_VALUE, id);
                        row.setProperty(FormUtil.PROPERTY_LABEL, label);
                    }
                }
            }
        }
        return results;
    }

    /**
     * Retrieves the Form object for a specific form ID.
     * @param formDefId
     * @return 
     */
    protected Form getSelectedForm(String formDefId) {
        Form form = null;
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        if (appDef != null && formDefId != null) {
            AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
            form = appService.viewDataForm(appDef.getId(), appDef.getVersion().toString(), formDefId, null, null, null, null, null, null);
        }
        return form;
    }

    @Override
    public String getDefaultPropertyValues() {
        return "";
    }

    @Override
    public String getFormBuilderTemplate() {
        return "";
    }
}