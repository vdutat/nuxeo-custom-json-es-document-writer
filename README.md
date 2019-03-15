# nuxeo-custom-json-es-document-writer

Plugin contributng a **JSON ES document writer** that retrieves the translated labels for the value stored in field `dc:nature` and add them in field `relatedtext:relatedtextresources` of the document's **JSON**. As field `relatedtext:relatedtextresources`'s value is copied to **elasticsearch**'s `all_field` (see **elasticsearch mapping**), these translated labels can be search with a full-text search.

The value of the document's field `relatedtext:relatedtextresources` is not changed in the database.

# Requirements

Building requires the following software:

* git
* maven

# Build

```
git clone ...
cd nuxeo-custom-json-es-document-writer

mvn clean install
```

# Installation

```
nuxeoctl mp-install nuxeo-custom-json-es-document-writer/nuxeo-custom-json-es-document-writer-package/target/nuxeo-custom-json-es-document-writer-package-*.zip
```

# Configuration

Contribute a **XML extension** in your bundle or define a **Studio XML extension** with the following **XML**:

## **XML contribution**

See `nuxeo-custom-json-es-document-writer/nuxeo-custom-json-es-document-writer-core/examples/jsonesdocumentwritercontrib-contrib.xml`.

## **Studio** XML extension

```
  <require>org.nuxeo.elasticsearch.ElasticSearchComponent.contrib</require>
  
  <extension target="org.nuxeo.elasticsearch.ElasticSearchComponent" point="elasticSearchDocWriter">

    <writer class="org.nuxeo.ecm.automation.jaxrs.io.documents.NatureDirLabelsJsonESDocumentWriter"/>
    
  </extension>
```

# Support

**These features are not part of the Nuxeo Production platform, they are not supported**

These solutions are provided for inspiration and we encourage customers to use them as code samples and learning resources.

This is a moving project (no API maintenance, no deprecation process, etc.) If any of these solutions are found to be useful for the Nuxeo Platform in general, they will be integrated directly into platform, not maintained here.


# Licensing

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)


# About Nuxeo

Nuxeo dramatically improves how content-based applications are built, managed and deployed, making customers more agile, innovative and successful. Nuxeo provides a next generation, enterprise ready platform for building traditional and cutting-edge content oriented applications. Combining a powerful application development environment with SaaS-based tools and a modular architecture, the Nuxeo Platform and Products provide clear business value to some of the most recognizable brands including Verizon, Electronic Arts, Sharp, FICO, the U.S. Navy, and Boeing. Nuxeo is headquartered in New York and Paris.

More information is available at [www.nuxeo.com](http://www.nuxeo.com).
