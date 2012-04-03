package edu.umn.enhs.xml

import javax.xml.stream.XMLStreamReader

class StaxCategory {
	static String prefix(XMLStreamReader self) {
		return self.prefix.toString()
	}
	static String localName(XMLStreamReader self) {
		return self.localName.toString()
	}
	static String name(XMLStreamReader self) {
		return self.name.toString()
	}
	static String text(XMLStreamReader self) {
		return self.elementText
	}
}
