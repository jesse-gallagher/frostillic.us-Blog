# frostillic.us Blog

This blog template is a Darwino-based Jakarta EE application.

## Server Requirements

The application requires a Java EE 8 and MicroProfile 3.0 container - it should work with any server that implements those, but has been developed and tested specifically with Open Liberty.

### Environment Configuration

- The application assumes the presence of a PostgreSQL DB configured in an external "darwino-beans.xml" file
  - It also assumes the presence of the PostgreSQL JDBC driver on the global classpath
- The application uses Akismet for comment spam detection, and this should be configured with the `frostillicus_blog.akismet-api-key` and `frostillicus_blog.akismet-blog` properties in "darwino.properties" or any location known to the MicroProfile Config API
- The Darwino debug console (for admin users) can be enabled by setting the `frostillicus_blog.dwo-runtime-debug-enable` property to `true` in "darwino.properties"

### JNDI Notes

If the database connection is set up via JNDI, then the Darwino bean should be configured to set the "objectTypeOther" driver property to "true", or else it will result in an error of "Can't infer the SQL type to use for an instance of org.postgresql.util.PGobject". For example:

```xml
<bean type="darwino/jsondb" name="frostillicus_blog" class="com.darwino.config.jsonstore.JsonDbJndi">
  <property name="db">postgresql</property>
  <property name="jndiPath">jdbc/postgresql</property>
  <map name="driverProperties">
    <entry key="objectTypeOther">true</entry>
  </map>
</bean>
```

### Open Liberty Notes

To run on Open Liberty or WLP, the following feature set is required at minimum:

```xml
<features>
    <feature>appSecurity-3.0</feature>
    <feature>beanValidation-2.0</feature>
    <feature>cdi-2.0</feature>
    <feature>jaxrs-2.1</feature>
    <feature>servlet-4.0</feature>
    <feature>jsonb-1.0</feature>
    <feature>jsp-2.3</feature>
    
    <feature>mpConfig-1.3</feature>
    <feature>mpRestClient-1.3</feature>
</features>
```

For JNDI database use, "jdbc-4-2" and "jndi-1.0" are also required. Alternatively, the "javaee-8.0" and "microprofile-3.0" features will cover everything.