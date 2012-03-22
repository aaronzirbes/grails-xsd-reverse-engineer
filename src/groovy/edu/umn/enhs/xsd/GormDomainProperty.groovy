package edu.umn.enhs.xsd

import grails.util.GrailsNameUtils
import groovy.util.slurpersupport.GPathResult
import javax.xml.bind.UnmarshalException

class GormDomainProperty {
	// name of domain property
	String name
	// name of column
	String columnName
	// nillable
	Boolean nullable = false
	// minOccurs
	Integer minOccurs
	// type or xs:simpleType
	String classType = 'String'
	// ncsdoc:pii
	String pii = false
	// ncsdoc:status
	Integer status
	// Minimum length
	Integer minLength
	// Maximum length
	Integer maxLength
	// restrictive pattern
	String pattern

	// default toString converter
	String toString() { name }

	/** constructor from xmlElement and known types */
	GormDomainProperty(GPathResult xmlElement, Collection<SimpleType> simpleTypes, Collection<EnumType> enumTypes) {
		def elementName = xmlElement?.name()
		if (elementName != 'element') {
			throw new UnmarshalException("expected element xs:element, got ${elementName}")
		} else {
			// get column name
			columnName = xmlElement.@name.text()
			// get nullable
			nullable = xmlElement.@nillable.text().toBoolean()
			// get minOccurs
			try {
				minOccurs = xmlElement.@minOccurs.text().toInteger()
			} catch (NumberFormatException ex) {
				minOccurs = null
			}
			// get PII info
			pii = xmlElement.'@ncsdoc:pii'.text()
			// get NCS status
			try {
				status = xmlElement.'@ncsdoc:status'.text().toInteger()
			} catch (NumberFormatException ex) {
				status = null
			}

			// convert column name to camel case
			name = GrailsNameUtils.getClassNameForLowerCaseHyphenSeparatedName(columnName.replace('_', '-'))
			// get data type
			def type = xmlElement.@type.text()
			// find type
			if (type) {
				EnumType enumType
				SimpleType simpleType
				// check simpleTypes
				if ( (simpleType = simpleTypes.find{ it.name == type }) ) {
					classType = simpleType.className
					(minLength, maxLength, pattern) = simpleType.contraints
				} else if ( (enumType = enumTypes.find{ it.tableName == type }) ) {
					classType = enumType.className
				}
			} else {
				//get simple type
				classType = 'String'
				// get constraits
				(minLength, maxLength, pattern) = XsdUtils.getConstraints(xmlElement.'xs:simpleType')
			}
		}
	}
}
