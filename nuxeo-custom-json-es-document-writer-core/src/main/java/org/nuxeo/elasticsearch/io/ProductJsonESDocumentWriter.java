package org.nuxeo.elasticsearch.io;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;

import com.fasterxml.jackson.core.JsonGenerator;

public class ProductJsonESDocumentWriter extends JsonESDocumentWriter {
	
	protected static final Log log = LogFactory.getLog(ProductJsonESDocumentWriter.class);
	
	protected static final String DOC_TYPE = "Product";
	protected static final String PROP_NAME = "product:Talent";
	protected String relatedText = "";

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
        if (DOC_TYPE.equals(doc.getType())) {
            CoreInstance.doPrivileged(doc.getRepositoryName(), (CoreSession session) -> {
                List<DocumentModel> relatedDocs = Arrays.stream((String[]) doc.getPropertyValue(PROP_NAME)).map(IdRef::new).map(session::getDocument).collect(Collectors.toList());
                if (log.isDebugEnabled()) {
                    for (DocumentModel document : relatedDocs) {
                        log.debug("Found talents: " + document.getPathAsString());
                    }
                }
                relatedText = relatedDocs.stream().map(document -> document.getTitle()).collect(Collectors.joining(" "));
                if (log.isDebugEnabled()) {
                    log.debug("Related text: " + relatedText);
                }
            });
        }
        for (String schema : schemas) {
            if (!relatedText.isEmpty() && RELATEDTEXT_SCHEMA.equals(schema)) {
                updateRelatedText(doc, PROP_NAME, relatedText);
            } 
            writeProperties(jg, doc, schema, null);
        }
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
        return propertyName + "::titles";
    }

}
