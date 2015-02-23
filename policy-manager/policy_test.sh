# Create endpoints
function addendpoint {
   json="{\"endpoint\": {
       \"id\":\"$1\",
       \"attributes\": [ "
   for ATT in $2 $3 $4 $5 $6 $7 $8 $9
   do
      json="$json { \"attribute\":\"$ATT\" },"
   done
   json="$json
       ] }
     }"

   curl -u admin:admin --noproxy localhost -X PUT --fail -ksSfL --url http://localhost:8181/restconf/config/policy-manager:endpoints/endpoint/$1 -H 'Content-Type: application/json' --data-binary "$json"
}

addendpoint 10.0.0.1 Bob employee infected
addendpoint 10.0.0.2 Carol contractor
addendpoint 1.2.3.4 DNSInspector server

# Create policies
curl -u admin:admin --noproxy localhost -X PUT --fail -ksSfL --url http://localhost:8181/restconf/config/policy-manager:domains/domain/domain-foo/application/app-bar/app-policy/policy-blah/policy -H 'Content-Type: application/json' --data-binary \
 '{"policy": {
   "action": {
     "type":"redirect",
     "data": [ {
        "name":"servicegroup",
        "value":"1.2.3.4"
     } ]
   },
   "classifier": {
     "expression" : [ {
       "id":0,
       "term" : [ {
         "type":"IP_PROTO",
         "value":"17"
       }, {
         "type":"L4_SRC",
         "value":"24"
       }, {
         "type":"L4_DST",
         "value":"25"
       } ]
     } ]
   },
   "source-endpoints": "infected",
   "destination-endpoints": "any"
 }} '

curl -u admin:admin --noproxy localhost -X PUT --fail -ksSfL --url http://localhost:8181/restconf/config/policy-manager:domains/domain/domain-foo/application/app-bar/app-policy/policy-bleh/policy -H 'Content-Type: application/json' --data-binary \
 '{"policy": {
   "action": {
     "type":"latency",
     "data": [ {
        "name":"latency_dscp",
        "value":"25"
     } ]
   },
   "classifier": {
     "expression" : [ {
       "id":0,
       "term" : [ {
         "type":"IP_PROTO",
         "value":"17"
       } ]
     } ]
   },
   "source-endpoints": "any",
   "destination-endpoints": "any"
 }} '


curl -u admin:admin --noproxy localhost -X DELETE --fail -ksSfL --url http://localhost:8181/restconf/config/policy-manager:domains/domain/domain-foo/application/app-bar/app-policy/policy-blah/policy 
curl -u admin:admin --noproxy localhost -X DELETE --fail -ksSfL --url http://localhost:8181/restconf/config/policy-manager:domains/domain/domain-foo/application/app-bar/app-policy/policy-bleh/policy 
