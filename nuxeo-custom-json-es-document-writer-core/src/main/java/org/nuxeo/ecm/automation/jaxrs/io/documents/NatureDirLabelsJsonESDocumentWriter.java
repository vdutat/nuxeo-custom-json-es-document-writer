package org.nuxeo.ecm.automation.jaxrs.io.documents;

import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;

@Provider
@Produces({ JsonESDocumentWriter.MIME_TYPE })
public class NatureDirLabelsJsonESDocumentWriter extends DirectoryLabelsJsonESDocumentWriter {

    @Override
    protected void setPropertiesToProcess() {
        addPropertyToProcess("dc:nature", "nature");
    }

}
