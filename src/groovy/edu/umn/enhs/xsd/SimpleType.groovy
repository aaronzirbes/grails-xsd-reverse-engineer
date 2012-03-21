package edu.umn.enhs.xsd

import groovy.util.slurpersupport.GPathResult
import javax.xml.bind.UnmarshalException
import grails.util.GrailsNameUtils

/** This represent an enum type pulled from XML data */
class SimpleType {

	/** name of XSD type */
	String name
	/** package class belongs to */
	String packageName = 'java.lang'
	/** name of class */
	String className = 'String'
	/** the minimum number of characters for the value */
	Integer minLength
	/** the maximum number of characters for the value */
	Integer maxLength
	/** the regexp the value must satisfy */
	String pattern
	/** default mapping helper */
	static Map classMap = [
		'int': [packageName: 'java.lang', className: 'Integer'],
		'decimal': [packageName: 'java.math', className: 'BigDecimal'],
		'date': [packageName: 'java.util', className: 'Date'],
		'time': [packageName: 'java.util', className: 'Date'],
		'dateTime': [packageName: 'java.util', className: 'Date']
		]

	/** a getter to return the full class path */
	String getClassPath() { "${packageName}.${className}" }

	/** the default toString converter */
	String toString() { "${classPath} ${name}" }

	/** Constructor to create a SimpleType from an XML element */
	SimpleType(GPathResult xmlElement) {

		def elementName = xmlElement?.name()
		if (elementName != 'simpleType') {
			throw new UnmarshalException("expected element xs:simpleType, got ${elementName}")
		} else {
			// set class attributes from xml element
			name = xmlElement.@name.text()

			// over-write class/package for certain types
			if (classMap[name]) {
				packageName = classMap[name].packageName
				className = classMap[name].className
			}

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
		}
	}

	static boolean isSimpleType(GPathResult xmlElement) {
		if (xmlElement.name() != 'simpleType') {
			return false
		} else if ( ! xmlElement.@name.text() ) {
			return false
		} else if ( xmlElement.'xs:restriction'.@base != 'xs:string' ) {
			return false
		} else if ( xmlElement.'xs:restriction'.'xs:enumeration'.list() ) {
			return false
		} else {
			return true
		}
	}
}
