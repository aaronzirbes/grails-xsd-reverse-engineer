package edu.umn.enhs.xsd

/** TODO: Rename to... DomainParser? */
class Parser {

	/** parse an XSD files definition data into a collection of classes */
	static parseAll(File xsdFile) {
		def xsdXml = new XmlSlurper().parse(xsdFile)


		// TODO: Load XSD Metadata
		def xsdMetaData = parseMetaData(xsdXml)

		// TODO: Load data types
		def xsdDataTypes = parseDataTypes(xsdXml)

		// TODO: Load Domain Classes
		def xsdDomainClasses = parseDomainClasses(xsdXml, xsdMetaData, xsdDataTypes)

		//return xsdXml
		return xsdMetaData
		//return xsdDataTypes
		//return xsdDomainClasses
	}

	static parseDataTypes(GPathResult xmlDoc){
		return []
	}

	static parseMetaData(GPathResult xmlDoc){
		return [:]
	}

	static parseDomainClasses(GPathResult xmlDoc, List xsdMetaData, Map xsdDataTypes) {
		return []
	}

}
