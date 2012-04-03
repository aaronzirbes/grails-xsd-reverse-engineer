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


USAGE = """
Usage: grails import-xml-data [--enums-as-constraint] <xsd-file-to-process>

Import XML data to domain classes created from XSD schema

    --enums-as-constraint    Will NOT create domain classes for enumeration types, but will instead create 'inList' constraints.

Example: grails xsd-to-gorm My-Company-Data.xml

    or

    grails xsd-to-gorm --enums-as-constraint Huge-Company-Data.xml
"""

//includeTargets << grailsScript('_GrailsInit')
includeTargets << grailsScript('_GrailsBootstrap')
//includeTargets << grailsScript('Compile')

xmlDataFilePath = ''
templateAttributes = [:]
appDir = "$basedir/grails-app"
String packageName = 'org.example'
Boolean strictXmlParsing = true
Boolean enumsAsDomainClasses = true

/** The primary target */
target(importXmlData: 'Generates domain classes from XSD file definition(s)') {
	depends(loadApp, configureApp)
	if ( ! configure() ) {
		return 1
	}
	String xmlSchema = 'http://www.w3.org/2001/XMLSchema'
	def nl = System.getProperty("line.separator")

	enumsAsDomainClasses = false

	def GormUtilities = classLoader.loadClass("edu.umn.enhs.xml.GormUtilities", true)
	// Instantiate the utility class and pass the grailsApplication instance
	def gormUtilities = GormUtilities.newInstance(grailsApp)

	if ( ! gormUtilities.getXmlToDomainClassMap() ) {
		errorMessage "Warning, no XSD enabled domain classes found!  Did you generate domain classes from an XSD document first?"
		return false
	}
	// Do some output magic...
	gormUtilities.printMessage = { String message -> event('StatusUpdate', [message]) }
	gormUtilities.errorMessage = { String message -> event('StatusError', [message]) }
	gormUtilities.finalMessage = { String message -> event('StatusFinal', [message]) }

	def xmlDataFile = new File(xmlDataFilePath)
	def xmlDataStream = xmlDataFile.newInputStream()

	finalMessage "Loading XML Domain Class information..."
	def xmlDomains = gormUtilities.loadXmlToDomainClassMap()

	finalMessage "Loading data from ${xmlDataFile}..."
	def elementsProcessed = gormUtilities.processXmlStream(xmlDataStream, strictXmlParsing)

	finalMessage "Finished loading ${elementsProcessed} elements from ${xmlDataFile}"
}

/** Helper for printing informational messages */
finalMessage = { String message -> event('StatusFinal', [message]) }
/** Helper for printing informational messages */
printMessage = { String message -> event('StatusUpdate', [message]) }
/** Helper for printing error messages */
errorMessage = { String message -> event('StatusError', [message]) }

/** Script configuration */
private boolean configure() {
	def argValues = parseArgs()
	if ( !argValues ) {
		return false
	} else if (argValues.size() == 1) {
		xmlDataFilePath = argValues[0]
		return true
	} else if (argValues.size() == 2 && argValues[0] == '--enums-as-constraint') {
		xmlDataFilePath = argValues[1]
		enumsAsDomainClasses = false
		return true
	}
}

/** Parse script arguments.
  * I stole this code from the Spring Security Core plugin.  Thanks Burt! */
private parseArgs() {
	def args = argsMap.params
	if (args.size() == 1) {
		printMessage "Creating domain classes from file: ${args[0]}"
		return args
	}

	errorMessage USAGE
	return null
}

setDefaultTarget 'importXmlData'
