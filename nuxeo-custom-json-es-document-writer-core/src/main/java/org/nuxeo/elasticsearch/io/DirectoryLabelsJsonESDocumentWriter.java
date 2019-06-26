package org.nuxeo.elasticsearch.io;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.i18n.I18NUtils;
import org.nuxeo.ecm.automation.features.PlatformFunctions;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.schema.FacetNames;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.ecm.core.schema.types.constraints.Constraint;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.services.config.ConfigurationService;

import com.fasterxml.jackson.core.JsonGenerator;

public abstract class DirectoryLabelsJsonESDocumentWriter extends JsonESDocumentWriter {

    protected static final Log log = LogFactory.getLog(NatureDirLabelsJsonESDocumentWriter.class);
    
    protected Map<String, String> relatedTexts = new HashMap<>();
    protected Map<String, String> propertiesToProcess = new HashMap<>();
    
    protected abstract void setPropertiesToProcess();

    private static final String RELATEDTEXT_RELATEDTEXTRESOURCES = "relatedtext:relatedtextresources";
    private static final String RELATEDTEXT_SCHEMA = "relatedtext";
    private static final String RELATEDTEXT_VALUE_KEY = "relatedtext";
    private static final String RELATEDTEXT_ID_KEY = "relatedtextid";

    @Override
    protected void writeSchemas(JsonGenerator jg, DocumentModel doc, String[] schemas) throws IOException {
        if (schemas == null || (schemas.length == 1 && "*".equals(schemas[0]))) {
            schemas = doc.getSchemas();
        }
        log.warn("writeSchemas " + doc.getPathAsString());
        setPropertiesToProcess();
        for (Entry<String, String> entry : propertiesToProcess.entrySet()) {
            String relatedText = computeRelatedText(doc, entry.getKey(), entry.getValue());
            relatedTexts.put(entry.getKey(), relatedText);
        }
        for (String schema : schemas) {
            if (RELATEDTEXT_SCHEMA.equals(schema)) {
                for (Entry<String, String> entry : relatedTexts.entrySet()) {
                    updateRelatedText(doc, entry.getKey(), entry.getValue());
                }
            }
            writeProperties(jg, doc, schema, null);
        }
    }

    protected String computeRelatedText(DocumentModel doc, String propertyName, String directoryName) {
        String relatedText = "";
        if (doc.hasSchema(getSchemaName(propertyName)) && doc.hasFacet(FacetNames.HAS_RELATED_TEXT)) {
            String prop = (String) doc.getPropertyValue(propertyName);
            if (!StringUtils.isEmpty(prop)) {
                PlatformFunctions fn = new PlatformFunctions();
                for (String lg : getLocales()) {
                    try {
                        relatedText += I18NUtils.getMessageString(Constraint.MESSAGES_BUNDLE, fn.getVocabularyLabel(directoryName, prop), new Object[0], Locale.forLanguageTag(lg)) + " ";
                        if (log.isDebugEnabled()) {
                            log.debug(Locale.forLanguageTag(lg) + " (" + lg + ")==>" + relatedText);
                        }
                    } catch (Exception e) {
                        log.warn("Unable to retrieve translated '" + lg + "' label in directory " + directoryName + " for key '" + prop + "' for field " + propertyName + " of document " + doc.getPathAsString());
                        if (log.isDebugEnabled()) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return relatedText;
    }

    @SuppressWarnings("unchecked")
    protected void updateRelatedText(DocumentModel doc, String propertyName, String value) {
        String relatedTextKey = getRelatedTextKey(propertyName);
        if (log.isDebugEnabled()) {
            log.debug("Updating " + relatedTextKey + " with '" + value + "'");
        }
        boolean updated = false;
        List<Map<String, String>> relatedtextList = (List<Map<String, String>>) doc.getPropertyValue(RELATEDTEXT_RELATEDTEXTRESOURCES);
        for (Map<String, String> relatedText : relatedtextList) {
            if (relatedTextKey.equals(relatedText.get(RELATEDTEXT_ID_KEY))) {
                relatedText.put(RELATEDTEXT_VALUE_KEY, value);
                if (log.isDebugEnabled()) {
                    log.debug(RELATEDTEXT_ID_KEY + " '" + relatedTextKey + "' updated with value " + RELATEDTEXT_VALUE_KEY + " '" + value + "'");
                }
                updated = true;
                break;
            }
        }
        if (!updated) {
            HashMap<String, String> newEntry = new HashMap<String, String>();
            newEntry.put(RELATEDTEXT_ID_KEY, relatedTextKey);
            newEntry.put(RELATEDTEXT_VALUE_KEY, value);
            relatedtextList.add(newEntry);
            if (log.isDebugEnabled()) {
                log.debug("Adding " + RELATEDTEXT_ID_KEY + " '" + relatedTextKey + "' updated with value " + RELATEDTEXT_VALUE_KEY + " '" + value + "'");
            }
        }
        doc.setPropertyValue(RELATEDTEXT_RELATEDTEXTRESOURCES, (Serializable) relatedtextList);
        if (log.isDebugEnabled()) {
            log.debug("Updated " + RELATEDTEXT_RELATEDTEXTRESOURCES + ": " + doc.getPropertyValue(RELATEDTEXT_RELATEDTEXTRESOURCES));
        }
    }
    
    /**
     * Returns the ID to use when storing the labels in field 'relatedtext:relatedtextresources'.
     * @return ID in 'relatedtext:relatedtextresources'
     */
    protected String getRelatedTextKey(String propertyName) {
        return propertyName + "::labels";
    }

    protected String getDefaultLanguages() {
        return "en-US,fr-FR,es-ES";
    }
    
    private String[] getLocales() {
        ConfigurationService cs = Framework.getService(ConfigurationService.class);
        return cs.getProperty("nuxeo.DirectoryLabelsJsonESDocumentWriter.languages", getDefaultLanguages()).split(",");
    }

    /**
     * Returns the schema name of a property.
     * @return the schema name
     */
    private String getSchemaName(String propertyName) {
        return Framework.getService(SchemaManager.class).getSchemaFromPrefix(propertyName.split(":")[0]).getName();
    }
    
    /**
     * Adds a property and its associated directory to the list of properties to process.
     * @param propertyName xpath of property
     * @param directoryName directory name
     * @since TODO
     */
    protected void addPropertyToProcess(String propertyName, String directoryName) {
        propertiesToProcess.put(propertyName, directoryName);
    }

}
