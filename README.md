WARNING
=======

This plugin was designed to reverse-engineer a specific set of XSD files.  Those XSD files are included in this source code for your reference.  This plugin may not work with the XSD file you need to parse, and may require some customization of the Groovy classes that are used to parse the XSD XML.

If this is the case, please do fork this project and customize or extend it for your application.  If you feel that your changes might help others, please submit a pull request so I can merge in your changes. Thanks!

In short: Your milage may vary.

Lessons Learned
---------------

The XML files I was parsing were huge, and generated over 600 domain classes.  Grails/Hibernate didn't like this, so I added an option to treat Enum types as constraints within the domain class rather than creating domain classes for each enum type.  This dropped my domain class count under 300, and significantly reduced the number of foreign key constraints.  It does mean you loose some of the GORM magic if you use the '--enums-as-constraint' option, but if you have a huge XSD file, you may have no choice.

If your XSD is small enough, skip the --enums-as-constraint option as then you'll be able to decode the enums nicely within your services/controllers/views.


Usage
-----

  grails xsd-to-gorm [--enums-as-constraint]  <path-to-xsd-file>

Example
-------

  grails xsd-to-gorm definitions/some-sort-of-xml-schema.0.01.02.xsd
