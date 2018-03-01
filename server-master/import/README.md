# Server-import module.
======

Server-import is a module of the component [server](..) capable of importing emails from terminal and .mbox files right to the MongoDB database.

### Configuration and running
To configure the import instance, you have change the information in the configuration files saved in the folder src/main/resource.
This folder consists of 3 files:
-	**database.properties:** Configuration file containing the information about the database instance
-	**mailinglists.properties:** Configuration file containing the information about the allowed mailinglists. When emails contains more addresses matching these mailinglists, duplicate for each of them will be saved in the database. If an email doesn't match any of the provided email addresses, the email will not be saved and the information will be logged to console.
-	**searchisko.properties:** Configuration file containing the information about the [searchisko](https://github.com/searchisko/searchisko) instance

Then run <pre><code>mvn clean install</code></pre> and use a script **import.sh** to import emails into the database instance. The script can be called
on a single mbox file or the whole folder containing the mbox files.
### Email format
As it was already said, emails are stored in the MongoDB database, so they are stored as BSON objects. The format of these messages looks like :

|  | | 
| :------------ |:---------------:|
| _id     | ID of the email |  
| replies     | Basic information about the emails that replied to this message. Attributes are: \_id,mailinglist, message\_id, subject, date, from, message\_snippet, tags    |  
| message\_id | MESSAGE\_ID information of the message       | 
| date | Number of type Long saving information about when the email was created       | 
| from | Email address of the sender       | 
| subject | The subject of the email address       | 
| from | Email address of the sender       | 
| main\_content | List of JSON objects storing information about the main content of the message. Each object in the list have attributes **type** (type of the message, like "text/plain", "text/html" etc.) and **text** (contains the information about the text data)     | 
| message\_snippet | The first chars of the whole message stored in the main\_content attribute.       | 
| mailinglist | Mailinglist to which the email belongs. Each email belong to only one mailinglist context.       | 
| in\_reply\_to |  Basic information about the email that this message replied to. Attributes are: \_id,mailinglist, message\_id, subject, date, from, message\_snippet, tags      | 
| thread\_root |  Basic information about the email that is the root of the thread to which this email belongs. Attributes are: \_id,mailinglist, message\_id, subject, date, from, message\_snippet, tags      | 
| email\_shard\_key | A string that is used for sharding (splitting data among more servers) when the MongoDB sharding is on. It is based on the mailinglist and ID attributes.   | 

#### Simple message example saved in the database
```
{ 
  "_id" : ObjectId("11...b"), 
  "replies" : [ ], 
  "message_id" : "<13819@webmail.messagingengine.com>", 
  "date" : NumberLong("1381950596000"), 
  "from" : "example@example.com", 
  "subject" : "Re: Example subject", 
  "main_content" : [
      { "type" : "text/plain", 	
        "text" : "Just a simple message." 
      } 
  ],
  "message_snippet" : "Just a simple message.", 
  "mailinglist" : "linux-cluster@redhat.com", 
  "in_reply_to" : 
      { "_id" : "53...22a", 
        "mailinglist" : "linux-cluster@redhat.com", 
        "message_id" : "<13...589@a.com>", 
        "subject" : "Example subject", 
        "date" : NumberLong("1381949041000"), 
        "from" : "example2@example2.com", 
        "message_snippet" : "A simple message number 2",
        "tags" : null 
     },
   "thread_root" : 
      {  "_id" : "53...22a", 
         "mailinglist" : "linux-cluster@redhat.com", 
         "message_id" : "<13...589@a.com>", 
         "subject" : "Example subject", 
         "date" : NumberLong("1381949041000"), 
         "from" : "example2@example2.com", 
         "message_snippet" : "Simple message number 2", 
         "tags" : null 
      }, 
    "email_shard_key" : "linux-cluster@redhat.com11...b" 
}
```
