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
Usage: grails import-xml-data [--enums-as-constraint] [--strict] <xml-file-to-process>

Import XML data to domain classes created from XSD schema

  --enums-as-constraint    Will NOT create domain classes for enumeration types, but will instead create 'inList' constraints.
  --strict                 Will throw an exception if unexpected elements are encountered with in a table/domain class.

Example:
  grails import-xml-data My-Company-Data.xml
    or
  grails import-xml-data --enums-as-constraint --strict Huge-Company-Data.xml
"""

//includeTargets << grailsScript('_GrailsInit')
includeTargets << grailsScript('_GrailsBootstrap')
//includeTargets << grailsScript('Compile')

xmlDataFilePath = ''
templateAttributes = [:]
appDir = "${basedir}/grails-app"
String packageName = 'org.example'
Boolean strictXmlParsing = true
Boolean enumsAsDomainClasses = true
File dataImportErrorLog = new File("${basedir}/target/xml-import-errors.csv")
PrintWriter errorLog = dataImportErrorLog.newPrintWriter()

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
	gormUtilities.logDataError = { recordNumber, className, fieldName, rejectedValue ->
		errorMessage "${it.className} rejected value '${rejectedValue}' for field ${fieldName}"
		errorLog.println "${recordNumber},\"${className}\",\"${fieldName}\",\"${rejectedValue}\""
	}

	def xmlDataFile = new File(xmlDataFilePath)

	finalMessage "Loading XML Domain Class information..."
	def xmlDomains = gormUtilities.loadXmlToDomainClassMap()

	def xmlDataStream = xmlDataFile.newInputStream()
	if ( ! dataImportErrorLog?.size() ) {
		// Write Column Headers
		errorLog.println "element_number,object_name,field_name,rejected_value"
	}

	finalMessage "Writing XML data import errors to file: ${dataImportErrorLog}"

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
	def allowedArgs = [ 
		'--enums-as-constraint',
		'--strict' ]
	def argSize = argValues.size()

	if ( !argValues ) {
		return false
	} else if (argSize == 1) {
		xmlDataFilePath = argValues[0]
		return true
	} else if (argSize > 1) {
		xmlDataFilePath = argValues[argSize - 1]
		argValues[1..-1].each{ arg ->
			if ( arg == '--enums-as-constraint' ) {
				enumsAsDomainClasses = false
			} else if ( arg == '--strict' ) {
				strict = true
			} else {
				errorMessage "Unknown argument: ${arg}"
				return false
			}
		}
		return true
	}
}

/** Parse script arguments.
  * I stole this code from the Spring Security Core plugin.  Thanks Burt! */
private parseArgs() {
	def args = argsMap.params
	if (args.size() == 1) {
		printMessage "Importing data to domain classes from file: ${args[0]}"
		return args
	}

	errorMessage USAGE
	return null
}

setDefaultTarget 'importXmlData'
