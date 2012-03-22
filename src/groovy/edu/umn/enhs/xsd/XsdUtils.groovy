package edu.umn.enhs.xsd

import groovy.util.slurpersupport.GPathResult

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

	/** Helper to get contstraint info from a simplType */
	static getConstraints(GPathResult xmlElement) {
		Integer minLength
		Integer maxLength
		String pattern
		def restriction = xmlElement.'xs:restriction'
		try {
			minLength = Integer.parseInt(restriction.'xs:minLength'.@value.text())
		} catch (NumberFormatException ex) {
			minLength = null
		}
		try {
			maxLength = Integer.parseInt(restriction.'xs:minLength'.@value.text())
		} catch (NumberFormatException ex) {
			maxLength = null
		}
		pattern = restriction.'xs:pattern'.@value.text()

		return [ minLength, maxLength, pattern ]
	}
}
