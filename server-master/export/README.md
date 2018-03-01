# Server-export module.
======

Server-export is a module of the component [server](..) capable of exporting all the emails from the MongoDB database. The email format that the database
is handling is documented in the module [import](../import)

### Configuration and running
To configure the export instance, you have change the information in the configuration files saved in the folder src/main/resource.
This folder consists of 3 files:
-	**database.properties:** Configuration file containing the information about the database instance
-	**mailinglists.properties:** Configuration file containing the information about the allowed mailinglists.
-	**searchisko.properties:** Configuration file containing the information about the [searchisko](https://github.com/searchisko/searchisko) instance

Then run <pre><code>mvn clean install</code></pre> and deploy the application to a WildFly instance.
### REST API
**GET METHODS**

URL address  | Response
------------- | -------------
rest/emails/all | Množina všetkých správ
rest/emails/email/{id} | Email with the given ID
rest/emails?from,mailinglist,tags,count,descending,ascending | Emails containing the given parameters being sorted by descending and ascending parameters.
/{mailinglist}/roots/count | Number of the roots of all the threads in the given {mailinglist}
rest/emails/{mailinglist}/roots/all | All the roots of the threads in the given {mailinglist}
rest/emails/{mailinglist}/roots?from,to | Emails in the given {mailinglist} that are in the range of numbers.\\ \hline
rest/mailinglists/all | List of all the mailinglists being processed on the server
rest/emails/search/content?content | Search emails using the given content string

**POST METHODS**

URL address  | Response
------------- | -------------
/email/tag/?id,tag | Add the given {tag} to the email with the given {id}

**DELETE METHODS**

URL address  | Response
------------- | -------------
/email/tag/?id,tag | Remove the given {tag} from the email with the given {id}
