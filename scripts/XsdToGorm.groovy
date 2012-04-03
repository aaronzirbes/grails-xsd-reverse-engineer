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
import groovy.text.SimpleTemplateEngine

USAGE = """
Usage: grails xsd-to-gorm [--enums-as-constraint] <xsd-file-to-process>

Creates domain classes from XSD file definition(s)

    --enums-as-constraint    Will NOT create domain classes for enumeration types, but will instead create 'inList' constraints.

Example: grails xsd-to-gorm My-Company-Schema-v1.2.3.4.xsd

    or

    grails xsd-to-gorm --enums-as-constraint Huge-Company-Schema-v5.4.3.2.xsd
"""

includeTargets << grailsScript('_GrailsInit')
includeTargets << grailsScript('_GrailsCreateArtifacts')
includeTargets << grailsScript('_GrailsBootstrap')
includeTargets << grailsScript('Compile')

xsdSourceFilePath = ''
templateAttributes = [:]
templateDir = "$xsdReverseEngineerPluginDir/src/templates"
appDir = "$basedir/grails-app"
templateEngine = new SimpleTemplateEngine()
String packageName = 'org.example'

Boolean enumsAsDomainClasses = true

/** The primary target */
target(xsdToGorm: 'Generates domain classes from XSD file definition(s)') {
	depends(classpath)
	depends(compile)
	if ( ! configure() ) {
		return 1
	}
	String xmlSchema = 'http://www.w3.org/2001/XMLSchema'
	def nl = System.getProperty("line.separator")

	enumsAsDomainClasses = false

	def GormParser = classLoader.loadClass("edu.umn.enhs.xsd.GormParser", true)
	def MetaData = classLoader.loadClass("edu.umn.enhs.xsd.MetaData", true)

	def xsdSourceFile = new File(xsdSourceFilePath)

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

	packageName = metaData.defaultPackageName
	templateAttributes.packageName = packageName
	String dir = packageToDir(packageName)

	if (enumsAsDomainClasses ) {
		// Generate the abstract class extended by all of the enumerations
		def templateFile = "$templateDir/XsdEnumerationDefinition.groovy.template"
		def destinationFile = "$appDir/domain/${dir}XsdEnumerationDefinition.groovy"
		generateFile templateFile, destinationFile

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
	} else {
		// Generate the abstract class extended by all of the enumerations
		def templateFile = "$templateDir/XsdEnumerationDefinition.groovy.template"
		def destinationFile = "$appDir/domain/${dir}XsdEnumerationDefinition.groovy"
		generateFile templateFile, destinationFile

		// Generate the class to store the enum definitions
		templateFile = "$templateDir/XsdEnumerationImpl.groovy.template"
		destinationFile = "$appDir/domain/${dir}XsdEnumerationImpl.groovy"
		generateFile templateFile, destinationFile
	}

	// dump out domain class information
	gormDomainList.each{ gormDomain ->
		printMessage "Creating ${gormDomain}"
		createArtifact(name: gormDomain.classPath, suffix: "", type: "DomainClass", path: "grails-app/domain")
		printMessage "Writing properties to ${gormDomain.pathName}"
		generateGormDomainFile gormDomain
	}

	// Create BootStrap data
	def bootStrapFile = new File('target/BootStrapXsd.groovy')
	generateBootstrapData bootStrapFile, enumTypeList

	// Create SQL BootStrap script
	def bootStrapScript = new File('target/bootstrap_xsd.sql')
	generateSqlBootstrap bootStrapScript, enumTypeList

}

/** This generates code you can put in your BootStrap to load required enum values */
generateSqlBootstrap = { bootStrapFile, enumTypeList -> 

	def nl = System.getProperty("line.separator")

	// Create BootStrap data
	def bsw = bootStrapFile.newWriter()
	printMessage "Creating: ${bootStrapFile}"
	enumTypeList.each{ enumType ->
		enumType.values.each{ enumValue ->
			if (enumsAsDomainClasses) {
				bsw << "INSERT INTO xsd_enumeration_definition${nl}"
				bsw << "  (class, value, label, master_cl, global_value, description)${nl}"
				bsw << "VALUES ('${enumType.classPath}', "
				bsw << "${enumValue.sqlValues});${nl}${nl}"
			} else { 
				bsw << "INSERT INTO xsd_enumeration_definition${nl}"
				bsw << "  (class, type_name, value, label, master_cl, global_value, description)${nl}"
				bsw << "VALUES ('${packageName}.XsdEnumerationImpl', "
				bsw << "'${enumType.tableName}', "
				bsw << "${enumValue.sqlValues});${nl}${nl}"
			}
		}
	}
	bsw.close()
	finalMessage "Created: ${bootStrapFile}"
	finalMessage "NOTE: You can run this SQL query by hand, or add code to the init"
	finalMessage "      section of your grails-app/conf/BootStrap.groovy file that"
	finalMessage "      will run it for you."
}

/** This generates code you can put in your BootStrap to load required enum values */
generateBootstrapData = { bootStrapFile, enumTypeList -> 

	def nl = System.getProperty("line.separator")

	// Get the Grails major version
	Integer grailsMajorVersion = 0
	try {
		grailsMajorVersion = grailsVersion[0].toInteger()
	} catch (NumberFormatException ex) {
		grailsMajorVersion = 1
	}

	// Create BootStrap data
	def bsw = bootStrapFile.newWriter()
	printMessage "Creating: ${bootStrapFile}"

	if (enumsAsDomainClasses) {
		enumTypeList.each{ enumType ->
			bsw << "import ${enumType.classPath}${nl}"
		}
	} else {
		bsw << "import ${packageName}.XsdEnumerationImpl"
	}
	bsw << "${nl}"
	def varNum = 0
	enumTypeList.each{ enumType ->
		enumType.values.each{ enumValue ->
			varNum++

			String varName = enumType.className.toLowerCase() 
			varName += "_${varNum}_" 
			varName += enumValue.value.toString().replaceAll('-', 'Neg')
			String typeClause = ", ${nl}\ttypeName:\"${enumType.tableName}\""

			if (grailsMajorVersion > 1) {
				// Grails 2.x code
				if (enumsAsDomainClasses) {
					bsw << "def ${varName} = ${enumType.className}"
					bsw << ".findOrSaveWhere(${enumValue.bootStrapCode})${nl}"
				} else {
					bsw << "def ${varName} = XsdEnumerationImpl"
					bsw << ".findOrSaveWhere(${enumValue.bootStrapCode}${typeClause})${nl}"
				}
			} else {
				// Grails 1.3.x code
				if (enumsAsDomainClasses) {
					bsw << "def ${varName} = ${enumType.className}"
					bsw << ".findWhere(${enumValue.bootStrapCode})${nl}"
					bsw << "if ( ! ${varName} ) {${nl}"
					bsw << "\t${varName} = new ${enumType.className}(${enumValue.bootStrapCode}).save()${nl}"
					bsw << "}${nl}"
				} else {
					bsw << "def ${varName} = XsdEnumerationImpl"
					bsw << ".findWhere(${enumValue.bootStrapCode}${typeClause})${nl}"
					bsw << "if ( ! ${varName} ) {${nl}"
					bsw << "\t${varName} = new XsdEnumerationImpl(${enumValue.bootStrapCode}${typeClause}).save()${nl}"
					bsw << "}${nl}"
				}
			}
		}
	}
	bsw.close()
	finalMessage "Created: ${bootStrapFile}"
	finalMessage "NOTE: If you want to have your app load the enumeration values on startup"
	finalMessage "      you can add the contents of ${bootStrapFile}"
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
	} else if (argValues.size() == 1) {
		xsdSourceFilePath = argValues[0]
		return true
	} else if (argValues.size() == 2 && argValues[0] == '--enums-as-constraint') {
		xsdSourceFilePath = argValues[1]
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

/** Generate a XSD GormDomain from a template. */
generateGormDomainFile = { gormDomain ->
	// Over-write the contents via newWriter() method (Thanks Guillaume!)
	def gormDomainFile = new File(gormDomain.pathName)
	String templatePath = "$templateDir/GormDomain.groovy.template"

	File templateFile = new File(templatePath)
	if (!templateFile.exists()) {
		errorMessage "\nERROR: $templatePath doesn't exist"
		return
	}

	// in case it's in a package, create dirs
	ant.mkdir dir: gormDomainFile.parentFile

	def model = [gormDomain: gormDomain, enumsAsDomainClasses: enumsAsDomainClasses ]

	gormDomainFile.withWriter { writer ->
		templateEngine.createTemplate(templateFile.text).make(model).writeTo(writer)
	}

	printMessage "generated ${gormDomainFile.absolutePath}"
}

/** Generate a file from a template.
  * I stole this code from the Spring Security Core plugin.  Thanks Burt! */
generateFile = { String templatePath, String outputPath ->

	File templateFile = new File(templatePath)
	if (!templateFile.exists()) {
		errorMessage "\nERROR: $templatePath doesn't exist"
		return
	}

	File outFile = new File(outputPath)

	// in case it's in a package, create dirs
	ant.mkdir dir: outFile.parentFile

	outFile.withWriter { writer ->
		templateEngine.createTemplate(templateFile.text).make(templateAttributes).writeTo(writer)
	}

	printMessage "generated ${outFile.absolutePath}"
}

/** This converts a package name to a folder path.
  * I stole this code from the Spring Security Core plugin.  Thanks Burt! */
packageToDir = { String pName ->
	String dir = ''
	if (pName) {
		dir = pName.replaceAll('\\.', '/') + '/'
	}

	return dir
}



setDefaultTarget 'xsdToGorm'
