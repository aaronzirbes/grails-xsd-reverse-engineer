package edu.umn.enhs.xsd

/** Some helper utilities for working with XSD files */
class XsdUtils {

	/** Helper to convert a name space to Java/Groovy package name */
	static String namespaceToPackage(String namespace) {
		return namespace.replace("http://www.","")
			.replace("http://","")
			.replace("https://www.","")
			.replace("https://","")
			.tokenize(".").reverse().join(".")
	}

}
