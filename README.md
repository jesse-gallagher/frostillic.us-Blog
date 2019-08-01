# frostillic.us Blog

This blog template is a Darwino-based Jakarta EE application.

## Server Requirements

The application requires a Java EE 8 and MicroProfile 3.0 container - it should work with any server that implements those, but has been developed and tested specifically with Open Liberty.

### Environment Configuration

- The application assumes the presence of a PostgreSQL driver configured in an external darwino-beans.xml file.
- The application uses Akismet for comment spam detection, and this should be configured with the `frostillicus_blog.akismet-api-key` and `frostillicus_blog.akismet-blog` properties in "darwino.properties" or any location known to the MicroProfile Config API