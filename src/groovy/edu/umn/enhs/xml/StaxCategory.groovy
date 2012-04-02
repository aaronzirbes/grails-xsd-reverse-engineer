package edu.umn.enhs.xml

import javax.xml.stream.XMLStreamReader

class StaxCategory {
	static Object get(XMLStreamReader self, String key) {
		return self.getAttributeValue(null, key)
	}
	static String name(XMLStreamReader self) {
		return self.name.toString()
	}
	static String text(XMLStreamReader self) {
		return self.elementText
	}
}
