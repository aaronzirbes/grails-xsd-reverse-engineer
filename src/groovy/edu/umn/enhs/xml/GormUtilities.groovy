package edu.umn.enhs.xml

import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamReader
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.commons.GrailsDomainClass
import org.springframework.validation.FieldError
import org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin

class GormUtilities {

	private final GrailsApplication grailsApplication
	private final ClassLoader classLoader

	// Default printMessage, errorMessage calls.
	// These can be (are) overwritten if needed
	def printMessage = { it -> println it }
	def finalMessage = { it -> println it }
	def errorMessage = { it -> println it }
	def logDataError = { recordNumber, className, fieldName, rejectedValue ->
		errorMessage "${it.objectName} rejected value '${rejectedValue}' for field ${fieldName}"
	}

	def propertyInstanceMap = DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP

	Integer transactionCount = 0
	Integer saveSuccessCount = 0
	Integer saveFailureCount = 0
	Integer transactionsPerFlush = 1000

	GormUtilities(GrailsApplication _grailsApplication) {
		grailsApplication = _grailsApplication;
		classLoader = grailsApplication.getClassLoader()

		loadXmlToDomainClassMap()
	}

	private Map xmlTablesToDomainClasses = [:]

	Map getXmlToDomainClassMap() {
		xmlTablesToDomainClasses
	}

	/** Returns all grails domain classes that can be parsed from XML */
	private loadXmlToDomainClassMap() {
		Map tableToClass = [:]

		def domainClasses = grailsApplication.domainClasses

		domainClasses.each{ GrailsDomainClass gdc ->
			try {
				def packageName = gdc.packageName
				def className = gdc.packageName + '.' + gdc.name
				def tableName = gdc.clazz.XSD_TABLE_NAME

				tableToClass[tableName] = className
			} catch (MissingPropertyException ex) {}
		}

		xmlTablesToDomainClasses = tableToClass

		return tableToClass
	}

	/** Processes an XML stream to import data from */
	def processXmlStream(InputStream inputStream, boolean strict) {
		def returnValue
		use (StaxCategory) {
			def startTime = System.currentTimeMillis()

			// process stream
			returnValue = processStaxStream(inputStream, strict)

			def endTime = System.currentTimeMillis()
			reportStatus()
			finalMessage "Finished running in ${endTime - startTime}ms"
		}
		return returnValue
	}

	/** Processes an XML stream in the context of a StaxCategory class */
	private processStaxStream(InputStream inputStream, boolean strict) {
		XMLStreamReader reader
		Integer elementReadCount = 0
		try {
			reader = XMLInputFactory.newInstance().createXMLStreamReader(inputStream)
			while (reader.hasNext()) {
				if (reader.startElement) {
					if ( processStartElement(reader, strict) ) {
						elementReadCount++
					}
				}
				reader.next()
			}
		} finally {
			reader?.close()
		}
		return elementReadCount
	}

	/** This processes an individual element in an XML data stream */
	private boolean processStartElement(element, boolean strict) {
		// Processing a com.ctc.wstx.sr.ValidatingStreamReader

		String elementName = element.localName()
		boolean readElement = false
		if (xmlTablesToDomainClasses.keySet().contains(elementName)) {
			readElement = true
			String tableName = elementName
			String className = xmlTablesToDomainClasses[tableName]
			printMessage "Marshalling ${className}"
			// create  a new instance using the class loader
			def XsdGormClass = classLoader.loadClass(className, true)
			// We need a transaction to ensure that there's a hibernate session
			XsdGormClass.withTransaction{
				// call the XMLStreamReader constructor for the class, strict XML
				def classInstance = XsdGormClass.newInstance(element, strict)
				if ( ! classInstance) {
					errorMessage "Unable to create new ${className}"
				} else {
					transactionCount++
					if ( ! classInstance.save() ) {
						saveFailureCount++
						errorMessage "Unable to save record #${transactionCount}, ${className}"
						classInstance.errors.allErrors.each{
							if (it instanceof FieldError) {
								logDataError transactionCount, className, it.field, it.rejectedValue
							} else {
								errorMessage it.toString()
							}
						}
					} else {
						printMessage "Imported data to ${className}"
						// Bump transaction value
						saveSuccessCount++
					}
					// Discard the domain class in Hibernate's session cache
					classInstance.discard()

					if (transactionCount % transactionsPerFlush == 0) {
						// flush the GORM cache to keep memory usage lower
						// as importing may create a LOT of objects. =)
						cleanUpGorm()
					}
				}
			}
		}
		return readElement
	}


	/** Flushes cache from GORM session.
	 * See: 
	 * <a href="http://naleid.com/blog/2009/10/01/batch-import-performance-with-grails-and-mysql/">This blog post</a>.
	 */
	private void cleanUpGorm() {
		reportStatus()
		def session = grailsApplication.mainContext.sessionFactory.currentSession
		session.flush()
		session.clear()
		propertyInstanceMap.get().clear()
	}
	
	private void reportStatus() {
		finalMessage "Processed ${transactionCount} records. ${saveSuccessCount} saved, ${saveFailureCount} failed with errors."
	}
}
