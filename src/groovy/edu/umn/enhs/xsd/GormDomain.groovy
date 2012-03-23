package edu.umn.enhs.xsd

import grails.util.GrailsNameUtils
import groovy.util.slurpersupport.GPathResult
import javax.xml.bind.UnmarshalException

class GormDomain {

	/** package class belongs to */
	String packageName
	/** name of enum class */
	String className
	/** tablename enum class should be stored in */
	String tableName

	Integer minRecords
	Integer maxRecords

	/** All know properties/columns for this domain class */
	Collection<GormDomainProperty> properties

	/** a getter to return the full class path */
	String getClassPath() { "${packageName}.${className}" }

	/** an alias for className */
	String getName() { className }

	/** the default toString converter */
	String toString() { classPath }

	/** Constructor from xmlElement and previously parsed data from the XML doc */
	GormDomain(GPathResult xmlElement, Collection<SimpleType> simpleTypes, Collection<EnumType> enumTypes) {

		def elementName = xmlElement?.name()
		if (elementName != 'element') {
			throw new UnmarshalException("expected element xs:element, got ${elementName}")
		} else {
			// get data from type
			tableName = xmlElement.@name.text()
			// convert tablename to camel case
			className = GrailsNameUtils.getClassNameForLowerCaseHyphenSeparatedName(tableName.replace('_', '-'))

			// load table wide constraints
			try {
				minRecords = Integer.parseInt(xmlElement.@minOccurs.text())
			} catch (NumberFormatException ex) {
				minRecords = null
			}
			try {
				maxRecords = Integer.parseInt(xmlElement.@maxOccurs.text())
			} catch (NumberFormatException ex) {
				maxRecords = null
			}

			// load the properties/columns
			properties = new ArrayList<GormDomainProperty>()
			xmlElement.'xs:complexType'.'xs:sequence'.'xs:element'.list().each{
				properties.add(new GormDomainProperty(it, simpleTypes, enumTypes))
			}
			// make sure there's at least one value
			if (! properties) {
				throw new UnmarshalException("GormDomain must have at least one xs:element in the sequence.")
			}
		}
	}

	static boolean isGormDomain(GPathResult xmlElement) {
		// TODO
		return true
	}

	String getPathName() {
		String basePath = 'grails-app/domain'
		String packagePath = packageName.replaceAll('\\.','/')
		String fileName = className + '.groovy'

		return "${basePath}/${packagePath}/${fileName}"
	}

	String generateClassDefinition() {
		def sb = new StringBuilder()
		def nl = System.getProperty("line.separator")

		sb << "package ${packageName}${nl}${nl}"
		sb << "/** Generated from Grails XSD plugin */${nl}"
		sb << "class ${className} {${nl}${nl}"
		properties.each{ p ->
			if (p.pii || p.status) {
				sb << "\t/**${nl}"
				if (p.pii) { sb << "\t * PII level ${p.pii}${nl}" }
				if (p.status) { sb << "\t * Status level ${p.status}${nl}" }
				sb << "\t */${nl}"
			}
			sb << "\t${p.classType} ${p.name}${nl}"
		}
		sb << "${nl}"
		sb << "\tstatic constraints = {${nl}"
		properties.each{ p ->
			sb << "\t\t${p.name}("
			sb << "nullable: ${p.nullable}"
			if (p.minLength) { sb << ", minSize:${p.minLength}" }
			if (p.maxLength) { sb << ", maxSize:${p.maxLength}" }
			if (p.pattern) { sb << ', matches:"' + p.pattern.replaceAll('\\\\',{'\\\\'}) + '"' }
			sb <<")${nl}"
		}
		sb << "\t}${nl}"
		sb << "\tstatic constraints = {${nl}"
		sb << "\t\ttable '${tableName}'${nl}"
		sb << "\t}${nl}"
		sb << "}${nl}"
		return sb
	}
}
