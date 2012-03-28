package edu.umn.enhs.xsd

import groovy.util.slurpersupport.GPathResult
import javax.xml.bind.UnmarshalException

/** This represent an enum type pulled from XML data */
class EnumType {

	/** package class belongs to */
	String packageName
	/** name of enum class */
	String className
	/** table name enum class should be stored in */
	String tableName
	/** description of enum type */
	String description
	/** Any enum values known for this type */
	Collection<EnumValue> values

	/** a getter to return the full class path */
	String getClassPath() { "${packageName}.${className}" }

	/** an alias for className */
	String getName() { className }

	/** the default toString converter */
	String toString() { 
		description ? "${classPath} - ${description}" : classPath
	}

	String getPathName() {
		String basePath = 'grails-app/domain'
		String packagePath = packageName.replaceAll('\\.','/')
		String fileName = className + '.groovy'

		return "${basePath}/${packagePath}/${fileName}"
	}

	// Create a constructor from an xmlElement
	EnumType(GPathResult xmlElement){
		def elementName = xmlElement?.name()
		if (elementName != 'simpleType') {
			throw new UnmarshalException("expected element xs:simpleType, got ${elementName}")
		} else {
			// get data from type
			tableName = xmlElement.@name.text()
			// convert table name to camel case
			className = XsdUtils.getClassName(tableName)
			// load the Enum Values
			values = new ArrayList<EnumValue>()
			xmlElement.'xs:restriction'.'xs:enumeration'.list().each{
				values.add(new EnumValue(it))
			}
			// make sure there's at least one value
			if (! values) {
				throw new UnmarshalException("EnumType must have at least one xs:enumeration value.")
			}
			// load the description from the first value
			description = values[0]?.description
		}
	}

	static boolean isEnumType(GPathResult xmlElement) {
		if (xmlElement.name() != 'simpleType') {
			return false
		} else if ( ! xmlElement.@name.text() ) {
			return false
		} else if ( xmlElement.'xs:restriction'.@base != 'xs:string' ) {
			return false
		} else if ( xmlElement.'xs:restriction'.'xs:enumeration'.list().size() < 1 ) {
			return false
		} else {
			return true
		}
	}

	String generateClassDefinition() {
		def sb = new StringBuilder()
		def nl = System.getProperty("line.separator")

		sb << "package ${packageName}${nl}${nl}"
		sb << "/** ${description} */${nl}"
		sb << "class ${className} extends XsdEnumerationDefinition { }${nl}"
		return sb
	}
}
