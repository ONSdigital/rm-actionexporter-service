# Service Improvements

The action exporter could easily be simplified and bought up to date.

1. Remove the use of XML/XSD and replace with JSON
1. Remove the Rest API endpoints for adding, updating and querying templates. These are not used in production and if we
require an additional template it would be easy enough to add directly to the database or...
1. potentially embed the templates inside the code where it can be version control and reviewed.
1. Add rest endpoints for retrigger the action exporter or create a mechanism to retry failed exports
1. The generated file should be persisted somewhere before it is sent to the SFTP server.
1. Remove the export report (filerowcount) table, it's not used and repeats information in the export file table. The 
row count could be stored there if its needed.
1. Remove the export job table, it's unnecessary.
1. Only one template is ever used but we have mechanism for using multiple and dynamically storing others. Is this actually needed?
1. Remove the message log table since the removal of the store procedures this hasn't been used since 2018.
1. Investigate removing the contact and address tables, they are hardly used and when this is actually needed it could
be looked up via the party service.


# Rewrite

This service lends itself nicely to a rewrite as it is isolated being an outer service and has a 
single distinct job and would massively benefit from simplification

The potential options would be:

1. Go using Go templates
2. Python using jninja templating

The other option is to upgrade this service to Spring Boot 2, move the freemarker templates into 
and simplify the follow as much as possible.