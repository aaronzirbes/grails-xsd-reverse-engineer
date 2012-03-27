class XsdReverseEngineerGrailsPlugin {
    // the plugin version
    def version = "0.1"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.0 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp",
        "grails-app/domain/**",
        "grails-app/conf/BootStrapXsd.groovy"
    ]

    // TODO Fill in these fields
    def title = "Xsd Reverse Engineering Plugin" // Headline display name of the plugin
    def author = "Aaron J. Zirbes"
    def authorEmail = "aaron.zirbes@gmail.com"
    def description = '''\
Convert XSD document to GORM objects
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/xsd-reverse-engineer"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
	def license = "GPL3"

    // Details of company behind the plugin (if there is one)
    def organization = [ name: "University of Minnesota", url: "http://www.enhs.umn.edu/" ]

    // Any additional developers beyond the author specified above.
//    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

    // Location of the plugin's issue tracker.
//    def issueManagement = [ system: "JIRA", url: "http://jira.grails.org/browse/GPMYPLUGIN" ]

    // Online location of the plugin's browseable source code.
//    def scm = [ url: "http://svn.grails-plugins.codehaus.org/browse/grails-plugins/" ]

}
