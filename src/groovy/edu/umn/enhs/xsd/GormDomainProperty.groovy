package edu.umn.enhs.xsd

import groovy.util.slurpersupport.GPathResult
import javax.xml.bind.UnmarshalException

class GormDomainProperty {
	// name of domain property
	String name
	// name of xml element
	String elementName
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
	// Flags whether or not this is an enumeration property
	Boolean enumProperty = false
	// inList constraint for enum types (if not rendered as domain classes)
	String inListConstraint

	// Static map to define xd: types as Java types
	static xdTypeMap = [
			string: 'String',
			int: 'Integer',
			bigint: 'BigInteger',
			boolean: 'Boolean',
			decimal: 'BigDecimal',
			float: 'Float',
			long: 'Long',
			double: 'Double'
		]
	
	static xdStringConversionMap = [
			'String': '',
			'Integer': '.toInteger()',
			'BigInteger': '.toBigInteger()',
			'Boolean': '.toBoolean()',
			'Decimal': '.toDecimal()',
			'BigDecimal': '.toBigDecimal()',
			'Float': '.toFloat()',
			'Long': '.toLong()', 
			'Double': '.toDouble()'
		]

	// default toString converter
	String toString() { name }

	// returns the data type used for this element
	String groovyType(boolean enumsAsDomainClasses) {
		if (enumProperty && !enumsAsDomainClasses) {
			"Integer ${name}"
		} else {
			"${classType} ${name}"
		}
	}

	String constraint(boolean enumsAsDomainClasses) {
		def sb = new StringBuilder()
		sb << "${name}("
		sb << "nullable: ${nullable}"
		if (!enumsAsDomainClasses && inListConstraint) { sb << ", inList:[${inListConstraint}]" }
		if (minLength) { sb << ", minSize:${minLength}" }
		if (maxLength) { sb << ", maxSize:${maxLength}" }
		if (pattern) { sb << ', matches:"' + pattern.replaceAll('\\\\',{'\\\\'}) + '"' }
		sb << ")"

		return sb.toString()
	}

	// The default String to native converter for this property
	String getStringConverter(boolean enumsAsDomainClasses) {
		if (enumProperty && !enumsAsDomainClasses) {
			".toInteger()"
		} else {
			xdStringConversionMap[classType] ?: ''
		}
	}

	/** constructor from xmlElement and known types */
	GormDomainProperty(GPathResult xmlElement, Collection<SimpleType> simpleTypes, Collection<EnumType> enumTypes) {
		def elemName = xmlElement?.name()
		if (elemName != 'element') {
			throw new UnmarshalException("expected element xs:element, got ${elemName}")
		} else {
			// get column name
			elementName = xmlElement.@name.text()
			// get nullable
			nullable = xmlElement.@nillable.text().toBoolean()
			// get minOccurs
			try {
				minOccurs = xmlElement.@minOccurs.text().toInteger()
			} catch (NumberFormatException ex) {
				minOccurs = null
			}

			// convert column name to camel case
			name = XsdUtils.getPropertyName(elementName)
			// Remove non-SQL names from column name
			columnName = XsdUtils.getColumnName(elementName)

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
						EnumType enumType = enumTypes.find{ it.tableName == typeName }
						if (enumType) {
							classType = enumType.className
							enumProperty = true
							inListConstraint = enumType.values.collect{it.value.toString()}.join(', ')
						}
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
				throw new UnmarshalException("unknown type: ${type} for ${elementName}")
			}
		}
	}
}
