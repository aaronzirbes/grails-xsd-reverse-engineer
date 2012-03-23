package edu.umn.enhs.xsd

import groovy.util.slurpersupport.GPathResult
import javax.xml.bind.UnmarshalException

/** This represent an enum type pulled from XML data */
class EnumValue {
	/** The integer value of this element */
	Integer value
	/** The label for this element */
	String label
	/** The master class name that this element belongs to */
	String masterClass
	/** a global value, if any, for this element */
	String globalValue
	/** description of enum type */
	String description

	String toString() { label }

	// Create a constructor from an xmlElement
	EnumValue(GPathResult xmlElement){
		def elementName = xmlElement?.name()
		if (elementName != 'enumeration') {
			throw new UnmarshalException("expected element xs:enumeration, got ${elementName}")
		} else {
			// set class attributes from xml element
			try {
				value = Integer.parseInt(xmlElement.@value.text())
			} catch (NumberFormatException ex) {
				value = null
			}
			label = xmlElement.'@ncsdoc:label'.text()
			description = xmlElement.'@ncsdoc:desc'.text()
			globalValue = xmlElement.'@ncsdoc:global_value'.text()
			masterClass = xmlElement.'@ncsdoc:master_cl'.text()
		}
	}

	String getBootStrapCode() {
		def nl = System.getProperty("line.separator")
		def fos = new StringBuilder()

		fos << "value: ${value}, ${nl}"
		fos << "\tlabel: \"${label.replaceAll('\\$',/\\\$/)}\", ${nl}"
		fos << "\tmasterClass: \"${masterClass}\", ${nl}"
		fos << "\tglobalValue: \"${globalValue}\", ${nl}"
		fos << "\tdescription: \"${description}\""
	}
}
