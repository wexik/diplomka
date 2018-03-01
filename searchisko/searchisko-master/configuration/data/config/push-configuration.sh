curl -X POST -d @search_fulltext_query_fields.json --user mlonline:admin http://localhost:8080/v2/rest/config/search_fulltext_query_fields --header "Content-Type:application/json"

curl -X POST -d @search_fulltext_highlight_fields.json --user mlonline:admin http://localhost:8080/v2/rest/config/search_fulltext_highlight_fields --header "Content-Type:application/json"

curl -X POST -d @search_response_fields.json --user mlonline:admin http://localhost:8080/v2/rest/config/search_response_fields --header "Content-Type:application/json" 

curl -X POST -d @search_fulltext_filter_fields.json mlonline:admin http://localhost:8080/v2/rest/config/search_fulltext_filter_fields --header "Content-Type:application/json" 

