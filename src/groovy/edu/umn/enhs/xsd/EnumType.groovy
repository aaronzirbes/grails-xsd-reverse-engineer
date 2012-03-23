package edu.umn.enhs.xsd

import grails.util.GrailsNameUtils
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
			className = GrailsNameUtils.getClassNameForLowerCaseHyphenSeparatedName(tableName.replace('_', '-'))
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
		sb << "class ${className} {${nl}${nl}"
		sb << "\t/** The numeric code for this enumeration */${nl}"
		sb << "\tInteger value${nl}"
		sb << "\t/** Textual representation of this value */${nl}"
		sb << "\tString label${nl}"
		sb << "\t/** The name of the Master Class for this enumeration */${nl}"
		sb << "\tString masterClass${nl}"
		sb << "\t/** The global value, if any */${nl}"
		sb << "\tString globalValue${nl}"
		sb << "\t/** Description of class */${nl}"
		sb << "\tString description${nl}"
		sb << "${nl}"
		sb << "\tstatic constraints = {${nl}"
		sb << "\t\tvalue(unique:true)${nl}"
		sb << "\t\tlabel(maxLength:255)${nl}"
		sb << "\t\tmasterClass(nullable:true, maxLength:255)${nl}"
		sb << "\t\tglobalValue(nullable:true, maxLength:255)${nl}"
		sb << "\t\tdescription(nullable:true, maxLength:255)${nl}"
		sb << "\t}${nl}"
		sb << "\tstatic constraints = {${nl}"
		sb << "\t\ttable '${tableName}'${nl}"
		sb << "\t\tmasterClass column: 'master_cl'${nl}"
		sb << "\t\tdescription column: 'desc'${nl}"
		sb << "\t}${nl}"
		sb << "}${nl}"
		return sb
	}
}
