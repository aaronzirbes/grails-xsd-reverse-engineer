package edu.umn.enhs.xsd

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
			className = XsdUtils.getClassName(tableName)

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
		return generateClassDefinition(true)
	}
}
