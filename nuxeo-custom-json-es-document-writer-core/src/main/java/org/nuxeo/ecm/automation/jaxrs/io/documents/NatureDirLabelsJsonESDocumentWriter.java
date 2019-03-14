package org.nuxeo.ecm.automation.jaxrs.io.documents;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonGenerator;
import org.nuxeo.common.utils.i18n.I18NUtils;
import org.nuxeo.ecm.automation.features.PlatformFunctions;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.schema.FacetNames;
import org.nuxeo.ecm.core.schema.types.constraints.Constraint;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.services.config.ConfigurationService;

@Provider
@Produces({ JsonESDocumentWriter.MIME_TYPE })
public class NatureDirLabelsJsonESDocumentWriter extends JsonESDocumentWriter {

    protected static final Log log = LogFactory.getLog(NatureDirLabelsJsonESDocumentWriter.class);
    
    private static final String RELATEDTEXT_RELATEDTEXTRESOURCES = "relatedtext:relatedtextresources";

    private static final String RELATEDTEXT_SCHEMA = "relatedtext";

    private static final String RELATEDTEXT_VALUE_KEY = "relatedtext";

    private static final String RELATEDTEXT_ID_KEY = "relatedtextid";
    
    @Override
    protected void writeSchemas(JsonGenerator jg, DocumentModel doc, String[] schemas) throws IOException {
        if (schemas == null || (schemas.length == 1 && "*".equals(schemas[0]))) {
            schemas = doc.getSchemas();
        }
        String relatedText = computeRelatedText(doc);
        for (String schema : schemas) {
            if (RELATEDTEXT_SCHEMA.equals(schema)) {
                if (log.isDebugEnabled()) {
                    log.debug("Updating " + getRelatedTextKey() + " with '" + relatedText + "'");
                }
                updateRelatedText(doc, relatedText);
            }
            writeProperties(jg, doc, schema, null);
        }
    }

    protected String computeRelatedText(DocumentModel doc) {
        String relatedText = "";
        if (doc.hasSchema(getSchemaName()) && doc.hasFacet(FacetNames.HAS_RELATED_TEXT)) {
            String prop = (String) doc.getPropertyValue(getPropertyName());
            if (!StringUtils.isEmpty(prop)) {
                PlatformFunctions fn = new PlatformFunctions();
                for (String lg : getLocales()) {
                    relatedText += I18NUtils.getMessageString(Constraint.MESSAGES_BUNDLE, fn.getVocabularyLabel(getDirectoryName(), prop), new Object[0], Locale.forLanguageTag(lg)) + " ";
                    if (log.isDebugEnabled()) {
                        log.debug(Locale.forLanguageTag(lg) + " (" + lg + ")==>" + relatedText);
                    }
                }
            }
        }
        return relatedText;
    }

    @SuppressWarnings("unchecked")
    protected void updateRelatedText(DocumentModel doc, String value) {
        boolean updated = false;
        List<Map<String, String>> relatedtextList = (List<Map<String, String>>) doc.getPropertyValue(RELATEDTEXT_RELATEDTEXTRESOURCES);
        for (Map<String, String> relatedText : relatedtextList) {
            if (getRelatedTextKey().equals(relatedText.get(RELATEDTEXT_ID_KEY))) {
                relatedText.put(RELATEDTEXT_VALUE_KEY, value);
                if (log.isDebugEnabled()) {
                    log.debug(RELATEDTEXT_ID_KEY + " '" + getRelatedTextKey() + "' updated with value " + RELATEDTEXT_VALUE_KEY + " '" + value + "'");
                }
                updated = true;
                break;
            }
        }
        if (!updated) {
            HashMap<String, String> newEntry = new HashMap<String, String>();
            newEntry.put(RELATEDTEXT_ID_KEY, getRelatedTextKey());
            newEntry.put(RELATEDTEXT_VALUE_KEY, value);
            relatedtextList.add(newEntry);
            if (log.isDebugEnabled()) {
                log.debug("Adding " + RELATEDTEXT_ID_KEY + " '" + getRelatedTextKey() + "' updated with value " + RELATEDTEXT_VALUE_KEY + " '" + value + "'");
            }
        }
        doc.setPropertyValue(RELATEDTEXT_RELATEDTEXTRESOURCES, (Serializable) relatedtextList);
        if (log.isDebugEnabled()) {
            log.debug("Updated " + RELATEDTEXT_RELATEDTEXTRESOURCES + ": " + doc.getPropertyValue(RELATEDTEXT_RELATEDTEXTRESOURCES));
        }
    }

    protected String[] getLocales() {
        ConfigurationService cs = Framework.getService(ConfigurationService.class);
        return cs.getProperty("nuxeo.DirectoryLabelsJsonESDocumentWriter.languages", getDefaultLanguages()).split(",");
    }

    protected String getDirectoryName() {
        return "nature";
    }

    protected String getPropertyName() {
        return "dc:nature";
    }

    protected String getSchemaName() {
        return "dublincore";
    }

    protected String getRelatedTextKey() {
        return "natureLabels";
    }

    protected String getDefaultLanguages() {
        return "en-US,fr-FR,es-ES";
    }

}
