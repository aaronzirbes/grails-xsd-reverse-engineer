package edu.umn.enhs.xsd

import groovy.util.slurpersupport.GPathResult
import grails.util.GrailsNameUtils

/** Some helper utilities for working with XSD files */
class XsdUtils {

	/** Helper to convert a name space to Java/Groovy package name */
	static String namespaceToPackage(String namespace) {
		return namespace.replace("http://www.","")
			.replace("http://","")
			.replace("https://www.","")
			.replace("https://","")
			.tokenize(".").reverse().join(".")
	}

	/** Helper to get contstraint info from a simplType */
	static getConstraints(GPathResult xmlElement) {
		Integer minLength
		Integer maxLength
		String pattern
		def restriction = xmlElement.'xs:restriction'
		try {
			minLength = Integer.parseInt(restriction.'xs:minLength'.@value.text())
		} catch (NumberFormatException ex) {
			minLength = null
		}
		try {
			maxLength = Integer.parseInt(restriction.'xs:maxLength'.@value.text())
		} catch (NumberFormatException ex) {
			maxLength = null
		}
		pattern = restriction.'xs:pattern'.@value.text()

		return [ minLength, maxLength, pattern ]
	}

	static Collection javaReservedWords = [
		'abstract', 'continue', 'for', 'new', 'switch',
		'assert', 'default', 'goto', 'package', 'synchronized',
		'boolean', 'do', 'if', 'private', 'this', 'break',
		'double', 'implements', 'protected', 'throw',
		'byte', 'else', 'import', 'public', 'throws', 'case',
		'enum', 'instanceof', 'return', 'transient', 'catch',
		'extends', 'int', 'short', 'try', 'char', 'final',
		'interface', 'static', 'void', 'class', 'finally',
		'long', 'strictfp', 'volatile', 'const', 'float',
		'native', 'super', 'while' ]

	static Collection sqlReservedWords = [
		'ACCESSIBLE', 'ADD', 'ALL', 'ALTER', 'ANALYZE', 
		'AND', 'AS', 'ASC', 'ASENSITIVE', 'BEFORE', 
		'BETWEEN', 'BIGINT', 'BINARY', 'BLOB', 'BOTH', 
		'BY', 'CALL', 'CASCADE', 'CASE', 'CHANGE', 
		'CHAR', 'CHARACTER', 'CHECK', 'COLLATE', 'COLUMN', 
		'CONDITION', 'CONSTRAINT', 'CONTINUE', 'CONVERT', 'CREATE', 
		'CROSS', 'CURRENT_DATE', 'CURRENT_TIME', 'CURRENT_TIMESTAMP', 'CURRENT_USER', 
		'CURSOR', 'DATABASE', 'DATABASES', 'DAY_HOUR', 'DAY_MICROSECOND', 
		'DAY_MINUTE', 'DAY_SECOND', 'DEC', 'DECIMAL', 'DECLARE', 
		'DEFAULT', 'DELAYED', 'DELETE', 'DESC', 'DESCRIBE', 
		'DETERMINISTIC', 'DISTINCT', 'DISTINCTROW', 'DIV', 'DOUBLE', 
		'DROP', 'DUAL', 'EACH', 'ELSE', 'ELSEIF', 
		'ENCLOSED', 'ESCAPED', 'EXISTS', 'EXIT', 'EXPLAIN', 
		'FALSE', 'FETCH', 'FLOAT', 'FLOAT4', 'FLOAT8', 
		'FOR', 'FORCE', 'FOREIGN', 'FROM', 'FULLTEXT', 
		'GRANT', 'GROUP', 'HAVING', 'HIGH_PRIORITY', 'HOUR_MICROSECOND', 
		'HOUR_MINUTE', 'HOUR_SECOND', 'IF', 'IGNORE', 'IN', 
		'INDEX', 'INFILE', 'INNER', 'INOUT', 'INSENSITIVE', 
		'INSERT', 'INT', 'INT1', 'INT2', 'INT3', 
		'INT4', 'INT8', 'INTEGER', 'INTERVAL', 'INTO', 
		'IS', 'ITERATE', 'JOIN', 'KEY', 'KEYS', 
		'KILL', 'LEADING', 'LEAVE', 'LEFT', 'LIKE', 
		'LIMIT', 'LINEAR', 'LINES', 'LOAD', 'LOCALTIME', 
		'LOCALTIMESTAMP', 'LOCK', 'LONG', 'LONGBLOB', 'LONGTEXT', 
		'LOOP', 'LOW_PRIORITY', 'MASTER_SSL_VERIFY_SERVER_CERT', 'MATCH', 'MAXVALUE', 
		'MEDIUMBLOB', 'MEDIUMINT', 'MEDIUMTEXT', 'MIDDLEINT', 'MINUTE_MICROSECOND', 
		'MINUTE_SECOND', 'MOD', 'MODIFIES', 'NATURAL', 'NOT', 
		'NO_WRITE_TO_BINLOG', 'NULL', 'NUMERIC', 'ON', 'OPTIMIZE', 
		'OPTION', 'OPTIONALLY', 'OR', 'ORDER', 'OUT', 
		'OUTER', 'OUTFILE', 'PRECISION', 'PRIMARY', 'PROCEDURE', 
		'PURGE', 'RANGE', 'READ', 'READS', 'READ_WRITE', 
		'REAL', 'REFERENCES', 'REGEXP', 'RELEASE', 'RENAME', 
		'REPEAT', 'REPLACE', 'REQUIRE', 'RESIGNAL', 'RESTRICT', 
		'RETURN', 'REVOKE', 'RIGHT', 'RLIKE', 'SCHEMA', 
		'SCHEMAS', 'SECOND_MICROSECOND', 'SELECT', 'SENSITIVE', 'SEPARATOR', 
		'SET', 'SHOW', 'SIGNAL', 'SMALLINT', 'SPATIAL', 
		'SPECIFIC', 'SQL', 'SQLEXCEPTION', 'SQLSTATE', 'SQLWARNING', 
		'SQL_BIG_RESULT', 'SQL_CALC_FOUND_ROWS', 'SQL_SMALL_RESULT', 'SSL', 'STARTING', 
		'STRAIGHT_JOIN', 'TABLE', 'TERMINATED', 'THEN', 'TINYBLOB', 
		'TINYINT', 'TINYTEXT', 'TO', 'TRAILING', 'TRIGGER', 
		'TRUE', 'UNDO', 'UNION', 'UNIQUE', 'UNLOCK', 
		'UNSIGNED', 'UPDATE', 'USAGE', 'USE', 'USING', 
		'UTC_DATE', 'UTC_TIME', 'UTC_TIMESTAMP', 'VALUES', 'VARBINARY', 
		'VARCHAR', 'VARCHARACTER', 'VARYING', 'WHEN', 'WHERE', 
		'WHILE', 'WITH', 'WRITE', 'XOR', 'YEAR_MONTH', 
		'ZEROFILL', 'GENERAL', 'IGNORE_SERVER_IDS', 'MASTER_HEARTBEAT_PERIOD', 'MAXVALUE', 
		'RESIGNAL', 'SIGNAL', 'SLOW' ]

	static String getClassName(String tableName) {
		// convert column name to camel case
		GrailsNameUtils.getClassNameForLowerCaseHyphenSeparatedName(tableName.replace('_', '-'))
	}

	static String getColumnName(String columnName) {
		def name
	   	if (sqlReservedWords.contains(name.toUpperCase())) {
			// rename it
			name += '_'
		}
		return name
	}
	

	static String getPropertyName(String columnName) {
		def name
		// convert column name to camel case
		name = GrailsNameUtils.getClassNameForLowerCaseHyphenSeparatedName(columnName.replace('_', '-'))
		// This usually works, but not for a few things... I don't know why.
		name = GrailsNameUtils.getPropertyNameRepresentation(name)
		// ... so we force the first character to be lowercase
		name = name.replaceFirst(name[0], name[0].toLowerCase())

		// Look for reserved Java words
		if (javaReservedWords.contains(name)) {
			// rename it
			name += '_'
		}
		return name
	}

}
