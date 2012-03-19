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

	// TODO: Parse XSD
	def xsdDefinitions = parseXsd(xsdSourceFile)
	// TODO: Create GORM domain classes
	createDomainClassesFromDefinitions(xsdDefinitions)
}

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
