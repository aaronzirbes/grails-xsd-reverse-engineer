package edu.umn.enhs.xsd

import groovy.util.slurpersupport.GPathResult

/** Renamed to... GormParser! */
class GormParser {

	/** parse an XSD files definition data into a collection of classes */
	static parse(File xsdFile) {
		// Read in an xsd file
		def xsdXml = new XmlSlurper().parse(xsdFile).declareNamespace(xs: 'http://www.w3.org/2001/XMLSchema')

		// Load XSD meta data
		MetaData metaData = new MetaData(xsdXml)

		// Load simple data types (date, string, boolean...)
		def xsdSimpleTypes = parseSimpleTypes(xsdXml)

		// Load enumeration types
		def xsdEnumDataTypes = parseEnumTypes(xsdXml, metaData)

		// Load Domain Classes
		def xsdDomainClasses = parseDomainClasses(xsdXml, metaData, xsdSimpleTypes, xsdEnumDataTypes)

		// TODO: Write domain classes

		// TODO: Write BootStrap code

		return xsdXml
	}

	static Collection<SimpleType> parseSimpleTypes(GPathResult xmlDoc){
		Collection<SimpleType> simpleTypeList = new ArrayList<SimpleType>()
		xmlDoc.'xs:simpleType'.findAll{ SimpleType.isSimpleType(it) }.each {
			simpleTypeList.add(new SimpleType(it))
		}
		return simpleTypeList
	}

	static Collection<SimpleType> parseEnumTypes(GPathResult xmlDoc, MetaData metaData){
		Collection<EnumType> enumTypeList = new ArrayList<EnumType>()
		xmlDoc.'xs:simpleType'.findAll{ EnumType.isEnumType(it) }.each {
			def enumTypeInstance = new EnumType(it)
			enumTypeInstance.packageName = metaData.defaultPackageName
			enumTypeList.add(enumTypeInstance)
		}
		return enumTypeList
	}

	static Collection findUnknownSimpleTypes(GPathResult xmlDoc) {
		def types = []
		xmlDoc.'xs:simpleType'.each{
			if (!EnumType.isEnumType(it) && !SimpleType.isSimpleType(it)) {
				def typeName = it.@name.text()
				types.add(typeName)
			}
		}
		return types
	}

	static parseDomainClasses(GPathResult xmlDoc, MetaData metaData, Collection xsdSimpleTypes, Collection xsdEnumDataTypes) {
		// Find all table-like elements
		// in english: 
		//   elements that are a complex type containing a sequence of elements with names that 
		//   themselves are only of standard or simple types.
		def tables = xmlDoc.'**'.grep{
			( it.name() == 'element'
			&& it.'xs:complexType'.'xs:sequence'.'xs:element'.@name.text().length()
			&& ( it.'xs:complexType'.'xs:sequence'.'xs:element'.@type.text().length()
				|| it.'xs:complexType'.'xs:sequence'.'xs:element'.'xs:simpleType'.size() ) ) }

		// Collect the tables and reverse-marshal them to objects
		Collection<GormDomain> gormDomains = new ArrayList<GormDomain>()
		tables.each{ table ->
			def gormDomainInstance = new GormDomain(table, xsdSimpleTypes, xsdEnumDataTypes)
			gormDomainInstance.packageName = metaData.defaultPackageName
			gormDomains.add(gormDomainInstance)
		}
		return gormDomains
	}
}
