package edu.umn.enhs.xsd

import groovy.util.slurpersupport.GPathResult

class MetaData {
	/** The xmlns attribute of the XML Schema */
	String xmlns
	/** The xmlns attribute of the XML Schema */
	String targetNamespace
	/** The xmlns attribute of the XML Schema */
	String elementFormDefault
	/** The xmlns attribute of the XML Schema */
	String attributeFormDefault

	/** The default string converter for this class */
	String toString() { targetNamespace }

	/** This is used to produce a java/groovy packagename from a targetNamespace */
	String getDefaultPackageName() {
		XsdUtils.namespaceToPackage(targetNamespace)
	}

	/** a constructor based on an XML document instance */
	MetaData(GPathResult xmlDoc) {
		xmlns = xmlDoc.@xmlns
		targetNamespace = xmlDoc.@targetNamespace
		elementFormDefault = xmlDoc.@elementFormDefault
		attributeFormDefault = xmlDoc.@attributeFormDefault 
	}
}
