package edu.umn.enhs.xsd

import grails.util.GrailsNameUtils
import groovy.util.slurpersupport.GPathResult
import javax.xml.bind.UnmarshalException

class GormDomainProperty {
	String name
	String columnName
	// nillable
	Boolean nullable = false
	// minOccurs
	Integer minOccurs
	// type or xs:simpleType
	String classType
	// ncsdoc:pii
	Boolean pii = false
	// ncsdoc:status
	Integer status
	// ncsdoc:key_asso
	String keyAssociation

	String toString() { name }

	GormDomainProperty(GPathResult xmlElement, Collection<SimpleType> simpleTypes, Collection<EnumType> enumTypes) {
		def elementName = xmlElement?.name()
		if (elementName != 'element') {
			throw new UnmarshalException("expected element xs:element, got ${elementName}")
		} else {
			// get column name
			columnName = xmlElement.@name.text()
			// convert column name to camel case
			name = GrailsNameUtils.getClassNameForLowerCaseHyphenSeparatedName(columnName.replace('_', '-'))
			// TODO: get type
			// TODO: get constraits
		}
	}
}
