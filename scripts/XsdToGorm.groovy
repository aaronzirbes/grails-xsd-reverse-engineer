/*
 * Copyright (C) 2012, Aaron J. Zirbes
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
import edu.umn.enhs.xsd.GormParser
import edu.umn.enhs.xsd.MetaData

USAGE = """
Usage: grails xsd-to-gorm <xsd-file-to-process>

Creates domain classes from XSD file definition(s)

Example: grails xsd-to-gorm My-Company-Schema-v0.2.3.4.xsd
"""

includeTargets << grailsScript('_GrailsBootstrap')

xsdSourceFilePath = ''

/** The primary target */
target(xsdToGorm: 'Generates domain classes from XSD file definition(s)') {
	depends(classpath)
	if ( ! configure() ) {
		return 1
	}

	def xsdSourceFile = new File(xsdSourceFilePath)

	// Parse the XML
	def xmlDoc = new XmlSlurper().parse(xsdSourceFile)
		.declareNamespace(xs: 'http://www.w3.org/2001/XMLSchema')
	// Get the info from the XML
	def metaData = new MetaData(xmlDoc)
	def simpleTypeList = GormParser.parseSimpleTypes(xmlDoc)
	def enumTypeList = GormParser.parseEnumTypes(xmlDoc, metaData)
	def gormDomainList = GormParser.parseDomainClasses(xmlDoc, metaData, sim:pleTypeList, enumTypeList)

	gormDomainList.each{ gormDomain ->
		println "Found domain: ${gormDomain}"
		gormDomain.properties.each{
			println "\t${it.name}"
		}
	}

	// TODO: Create GORM domain classes
	// createDomainClassesFromDefinitions(xsdDefinitions)
}

/** Helper for printing informational messages */
printMessage = { String message -> event('StatusUpdate', [message]) }
/** Helper for printing error messages */
errorMessage = { String message -> event('StatusError', [message]) }

/** Script configuration */
private boolean configure() {
	def argValues = parseArgs()
	if ( !argValues ) {
		return false
	} else {
		xsdSourceFilePath = argValues[0]
		return true
	}
}

/** Parse script arguments */
private parseArgs() {
	def args = argsMap.params
	if (args.size() == 1) {
		printMessage "Creating domain classes from file: ${args[0]}"
		return args
	}

	errorMessage USAGE
	return null
}

setDefaultTarget 'xsdToGorm'
