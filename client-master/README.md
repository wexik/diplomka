# Client component

The client component is responsible for showing the emails in a human readable format. Since the communication with the server is based on the REST only, there can be more clients created. 


#### Used libraries/technologies

- JSF
- PrettyFaces
- MongoDB

#### Deployment to application server
Application is using the container-based security with the MongoDB database. Since the MongoDB login module is not part of the default modules, the one for MongoDB had to be created. To let it work, you must add block of XML to register it inside the application server. Add this XML snippet to the standalone.xml configuration file of the application server:

```
 		<security-domain name="mongo_auth" cache-type="default">
                    <authentication>
                        <login-module code="com.redhat.mailinglistOnline.security.MongoDbLoginModule" flag="required">
                            <module-option name="hashAlgorithm" value="SHA-256"/>
                        </login-module>
                    </authentication>
                </security-domain>
```
