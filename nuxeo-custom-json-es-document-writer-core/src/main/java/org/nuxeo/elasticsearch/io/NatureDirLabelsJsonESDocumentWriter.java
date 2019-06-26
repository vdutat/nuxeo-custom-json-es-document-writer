package org.nuxeo.elasticsearch.io;

public class NatureDirLabelsJsonESDocumentWriter extends DirectoryLabelsJsonESDocumentWriter {

    @Override
    protected void setPropertiesToProcess() {
        addPropertyToProcess("dc:nature", "nature");
    }

}
