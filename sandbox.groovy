import edu.umn.enhs.xsd.Parser

def f = new File('definitions/NCS_Transmission_Schema_2.0.01.02.xsd')

def xmlDoc = Parser.parse(f)

