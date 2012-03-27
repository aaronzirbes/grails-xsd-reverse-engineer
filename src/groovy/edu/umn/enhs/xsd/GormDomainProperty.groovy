package edu.umn.enhs.xsd

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
	String classType
	// Minimum length
	Integer minLength
	// Maximum length
	Integer maxLength
	// restrictive pattern
	String pattern
	// standard XML schema type map
	static xdTypeMap = [
			string: 'String',
			int: 'Integer',
			boolean: 'Boolean',
			decimal: 'BigDecimal',
			float: 'Float',
			double: 'Double'
		]

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

			// convert column name to camel case
			name = XsdUtils.getPropertyName(columnName)

			// get data type
			def type = xmlElement.@type.text()
			// find type
			// Basic Types first... (TODO, check spec.  I'm probably missing some)
			if (type) {
				// check simpleTypes
				String typeName = ''
				String schemaName = ''
				(schemaName, typeName) = type.tokenize(':')

				if (schemaName == 'xs') {
					classType = xdTypeMap[typeName]
				} else {
					// Lookup the type
					def simpleType = simpleTypes.find{ it.name == typeName }
					if ( simpleType ) { 
						classType = simpleType.className
						(minLength, maxLength, pattern) = simpleType.constraints
					} else {
						// we only need the classType for an Enum type
						classType = enumTypes.find{ it.tableName == typeName }?.className
					}
				}
			} else {
				//get simple type
				classType = 'String'
				// get constraits
				(minLength, maxLength, pattern) = XsdUtils.getConstraints(xmlElement.'xs:simpleType')
			}

			// Throw an error if we couldn't find the type
			if (! classType) {
				throw new UnmarshalException("unknown type: ${type} for ${columnName}")
			}
		}
	}
}
