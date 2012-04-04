class XsdReverseEngineerGrailsPlugin {
    // the plugin version
    def version = "0.3.0"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.3 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/**",
        "grails-app/domain/**",
        "definitions/**",
        "web-app/**"
    ]

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
    def developers = [ [ name: "Aaron J. Zirbes", email: "ajz@umn.edu" ]]

    // Location of the plugin's issue tracker.
    def issueManagement = [ system: "GitHub", url: "http://jira.grails.org/browse/GPMYPLUGIN" ]

    // Online location of the plugin's browseable source code.
    def scm = [ url: "https://github.com/aaronzirbes/grails-xsd-reverse-engineer" ]

}
