WARNING
=======
This plugin was designed to reverse-engineer a specific set of XSD files.  Those XSD files are included in this source code for your reference.  This plugin may not work with the XSD file you need to parse, and may require some customization of the Groovy classes that are used to parse the XSD XML.

If this is the case, please do fork this project and customize or extend it for your application.  If you feel that your changes might help others, please submit a pull request so I can merge in your changes. Thanks!

In short: Your milage may vary.

Lessons Learned
---------------
The XML files I was parsing were huge, and generated over 600 domain classes.  Grails/Hibernate didn't like this, so I added an option to treat Enum types as constraints within the domain class rather than creating domain classes for each enum type.  This dropped my domain class count under 300, and significantly reduced the number of foreign key constraints.  It does mean you loose some of the GORM magic if you use the '--enums-as-constraint' option, but if you have a huge XSD file, you may have no choice.

If your XSD is small enough, skip the --enums-as-constraint option as then you'll be able to decode the enums nicely within your services/controllers/views.


Usage (Structure Creation) 
--------------
    grails xsd-to-gorm [--enums-as-constraint]  <path-to-xsd-file>

Example (Structure Creation)
----------------
    grails xsd-to-gorm definitions/some-sort-of-xml-schema.0.01.02.xsd

Usage (Import) 
--------------
    grails import-xml-data [--enums-as-constraint] [--strict] <path-to-xsd-file>

Example (Import)
----------------
    grails import-xml-data definitions/xml-data-based-on-schema.0.01.02.xml


Performance Tuning
------------------
The following settings can help your application run faster while importing large datasets.

Many of these settings were drawn from the following blog posts:
* [feeldr](http://memo.feedlr.com/?p=31)
* [techdm](http://techdm.com/grails/?p=87&lang=en)
* [An Army of Solipsists](http://burtbeckwith.com/blog/?p=73)

1. Reduce the JVM heap size, and turn on the parallel garbage collector

    export GRAILS_OPTS="-Xmx512m -Xms512m -XX:PermSize=128m -XX:MaxPermSize=128m -XX:+UseParallelOldGC"

2. Turn off hibernate's 2nd level cache in your DataSource config
    hibernate {
        cache.use_second_level_cache = false
        cache.use_query_cache = false
    }


