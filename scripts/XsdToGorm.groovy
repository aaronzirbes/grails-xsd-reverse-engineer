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

includeTargets << grailsScript('_GrailsInit')
includeTargets << grailsScript('_GrailsCreateArtifacts')
includeTargets << grailsScript('_GrailsBootstrap')
includeTargets << grailsScript('Compile')

xsdSourceFilePath = ''

/** The primary target */
target(xsdToGorm: 'Generates domain classes from XSD file definition(s)') {
	depends(classpath)
	depends(compile)
	if ( ! configure() ) {
		return 1
	}
	String xmlSchema = 'http://www.w3.org/2001/XMLSchema'
	def nl = System.getProperty("line.separator")

	def GormParser = classLoader.loadClass("edu.umn.enhs.xsd.GormParser", true)
	def MetaData = classLoader.loadClass("edu.umn.enhs.xsd.MetaData", true)

	def xsdSourceFile = new File(xsdSourceFilePath)

	// Get the Grails major version
	Integer grailsMajorVersion = 0
	try {
		grailsMajorVersion = grailsVersion[0].toInteger()
	} catch (NumberFormatException ex) {
		grailsMajorVersion = 1
	}
	// Parse the XML
	def xmlDoc = new XmlSlurper().parse(xsdSourceFile)
		.declareNamespace(xs: xmlSchema)
	// Get the info from the XML
	def metaData = MetaData.newInstance(xmlDoc)
	printMessage "Loaded metadata for ${metaData.targetNamespace}"
	def simpleTypeList = GormParser.parseSimpleTypes(xmlDoc)
	printMessage "Loaded (${simpleTypeList.size()}) simpleType classes"
	def enumTypeList = GormParser.parseEnumTypes(xmlDoc, metaData)
	printMessage "Loaded (${enumTypeList.size()}) enumeration simpleType classes"
	// check for unknown simpleTypes
	def unknownSimpleTypes = GormParser.findUnknownSimpleTypes(xmlDoc)
	if (unknownSimpleTypes) {
		unknownSimpleTypes.each {
			errorMessage "Unknown simpleType: ${it}"
		}
		return false
	}
	// Load tables/domain classes
	def gormDomainList = GormParser.parseDomainClasses(xmlDoc, metaData, simpleTypeList, enumTypeList)

	// dump out enumeration domain class information
	enumTypeList.each{ enumType ->
		printMessage "Creating ${enumType.classPath}"
		createArtifact(name: enumType.classPath, suffix: "", type: "DomainClass", path: "grails-app/domain")
		printMessage "Writing properties to ${enumType.pathName}"
		// Over-write the contents via newWriter() method (Thanks Guillaume!)
		def gormDomainFile = new File(enumType.pathName).newWriter()
		gormDomainFile << enumType.generateClassDefinition()
		gormDomainFile.close()
	}

	// dump out domain class information
	gormDomainList.each{ gormDomain ->
		printMessage "Creating ${gormDomain}"
		createArtifact(name: gormDomain.classPath, suffix: "", type: "DomainClass", path: "grails-app/domain")
		printMessage "Writing properties to ${gormDomain.pathName}"
		// Over-write the contents via newWriter() method (Thanks Guillaume!)
		def gormDomainFile = new File(gormDomain.pathName).newWriter()
		gormDomainFile << gormDomain.generateClassDefinition()
		gormDomainFile.close()
	}

	// Create BootStrap data
	def bootStrapFile = new File('grails-app/conf/BootStrapXsd.groovy')
	def bsw = bootStrapFile.newWriter()
	printMessage "Creating: ${bootStrapFile}"
	enumTypeList.each{ enumType ->
		bsw << "import ${enumType.classPath}${nl}"
	}
	bsw << "${nl}"
	enumTypeList.each{ enumType ->
		enumType.values.each{ enumValue ->
			def varName = enumType.className.toLowerCase() + enumValue.value.toString().replaceAll('-', 'Neg')
			if (grailsMajorVersion > 1) {
				// Grails 2.x code
				bsw << "def ${varName} = ${enumType.className}"
				bsw << ".findOrSaveWhere(${enumValue.bootStrapCode})${nl}"
			} else {
				// Grails 1.3.x code
				bsw << "def ${varName} = ${enumType.className}"
				bsw << ".findWhere(${enumValue.bootStrapCode})${nl}"
				bsw << "if ( ! ${varName} ) {${nl}"
				bsw << "\t${varName} = new ${enumType.className}(${enumValue.bootStrapCode}).save()${nl}"
				bsw << "}${nl}"
			}
		}
	}
	bsw.close()
	finalMessage "Created: ${bootStrapFile}"
	finalMessage "NOTE: Don't forget to add the contents of ${bootStrapFile}"
	finalMessage "      to the init section of your grails-app/conf/BootStrap.groovy file."

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
