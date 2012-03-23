package edu.umn.enhs.xsd

import groovy.util.slurpersupport.GPathResult
import grails.util.GrailsNameUtils

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
			maxLength = Integer.parseInt(restriction.'xs:maxLength'.@value.text())
		} catch (NumberFormatException ex) {
			maxLength = null
		}
		pattern = restriction.'xs:pattern'.@value.text()

		return [ minLength, maxLength, pattern ]
	}

	static Collection javaReservedWords = [
		'abstract', 'continue', 'for', 'new', 'switch',
		'assert', 'default', 'goto', 'package', 'synchronized',
		'boolean', 'do', 'if', 'private', 'this', 'break',
		'double', 'implements', 'protected', 'throw',
		'byte', 'else', 'import', 'public', 'throws', 'case',
		'enum', 'instanceof', 'return', 'transient', 'catch',
		'extends', 'int', 'short', 'try', 'char', 'final',
		'interface', 'static', 'void', 'class', 'finally',
		'long', 'strictfp', 'volatile', 'const', 'float',
		'native', 'super', 'while' ]

	static String getClassName(String tableName) {
		// convert column name to camel case
		GrailsNameUtils.getClassNameForLowerCaseHyphenSeparatedName(tableName.replace('_', '-'))
	}

	static String getPropertyName(String columnName) {
		def name
		// convert column name to camel case
		name = GrailsNameUtils.getClassNameForLowerCaseHyphenSeparatedName(columnName.replace('_', '-'))
		// This usually works, but not for a few things... I don't know why.
		name = GrailsNameUtils.getPropertyNameRepresentation(name)
		// ... so we force the first character to be lowercase
		name = name.replaceFirst(name[0], name[0].toLowerCase())

		// Look for reserved Java words
		if (javaReservedWords.contains(name)) {
			// rename it
			name += '_'
		}
		return name
	}

}
