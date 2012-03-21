import edu.umn.enhs.xsd.GormParser
import edu.umn.enhs.xsd.MetaData

def f = new File('definitions/NCS_Transmission_Schema_2.0.01.02.xsd')
println "Reading ${f.name}..."

def xmlDoc = new XmlSlurper().parse(f).declareNamespace(xs: 'http://www.w3.org/2001/XMLSchema')

def metaData = new MetaData(xmlDoc)
def simpleTypeList = GormParser.parseSimpleTypes(xmlDoc)
def enumTypeList = GormParser.parseEnumTypes(xmlDoc, metaData)
def gormDomainList = GormParser.parseDomainClasses(xmlDoc, metaData, simpleTypeList, enumTypeList)

gormDomainList.each{ gormDomain ->
    println "Found domain: ${gormDomain}"
    gormDomain.properties.each{
        println "\t${it.name}"
    }
}

true