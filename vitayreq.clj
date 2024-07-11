(config 

(text-field 

:name "username"
:label "Secret Token"
:placeholder "Enter API key generated with vitally"
:required true
)

(hidden-field 
:name "password"
:placeholder ""
)

(text-field 
:name "url"   ; This should be accordingly to label.
:label "Url"
:placeholder "enter the Domain Url"
:required true
))
(default-source
    (http/get :base-url {url})     ; correct it. this is wrong
    (header-params "Accept" "application/json")
    (auth/http-basic)
    (pagination/key-based 
    :scroll-key-query-param-name "from"    
    :scroll-value-path-in-response "next"
    :limit 100   ; give the limit name parameter.
    :limit-query-param-name "limit"
    )
    (error-handler 
    (when :status 400 :action fail)
    (when :status 401 :action fail)
    (when :status 429 :action rate-limit)
    ))

(entity ORGANIZATION 
(source 
(http/get :url "/resources/organizations")
(extract-path "results")
(setup-test 
(upon-receiving :code 200(pass))))
(fields 
id :id
created_at :<="createdAt"
updated_at :<="updatedAt"
external_id :<="externalId"
organization_owenr_id :<="oraganizationOwnerId"
churned_at :<="churnedAt"
name
mrr 
next_renewal_date:<="nextRenwalDate"
trail_end_date:<="traiEndDate"
user_count:<="userCount"
csm_id:<="csmId"
account_exicutive_id:<="accountExecutiveId" 
accounts_count:<="accountsCount"
health_score:<="healthScore"
(dynamic-fields
(flatten-fields
(fields 
id 
created_at:<="createdAt"
label
path
type
options)
:from "traits" )))    ; "traits"
(relate 
(contains-list-of KEY_ROLE_ORG :inside-prop "keyRoles") ; please takecare of the naming conventions
(contains-list-of SEGMENTS_ORG :inside-prop "segments"))) ; same

(entity SEGMENTS_ORG
(fields 
id :id
name
)  
(relate
(needs ORGANIZATION :prop "id"))       ; missing needs
)

(entity KEY_ROLE_ORG
(fields 
index :id :index
vitally_user:<="vitallyUser"  ; missing PK
key_role:<="keyRole")
(relate(needs ORGANIZATION prop:"id"))
)
   ; missing the fields in the organization endpoint
(entity ACCOUNT 
(source 
(http/get :url "/resources/accounts")
(extract-path "results"))
(fields 
id :id
created_at:<="createdAt"
updated_at:<="updatedAt"
external_id:<="externalID"
name
organization_id:<="organizationId"
account_owner_id:<="accountOwnerId"
churned_at:<="churnedAt"
first_seen_time_stamp:<="firstSeenTimeStamp"
last_seen_time_stamp:<="lastSeenTimeStamp"
last_inbound_message_timestamp:<="lastInboundMessageTimestamp"
last_outbound_message_timestamp:<="lastOutboundMessageTimestamp"
mrr
next_renewal_date:<="nextRenewalDate"
trial_end_date:<="trialEndDate"
users_count:<="usersCount"
nps_detractor_count:<="npsDetractorCount"
nps_passive_count:<="npsPassiveCount"
nps_promoter_count:<="npsPromoterCount"
nps_score:<="npsScore" 
health_score:<="healthScore"
csm_id:<="csmId" 
account_executive_id:<="accountExecutiveId" 
)
(dynamic-fields
(flatten-fields
(fields 
id :id     ; remove this
created_at:<="createdAt"
label
path
type
options)
:from "traits"))  ; this should be in quotes
(relate 
(contains-list-of ACCOUNT_KEY_ROLE :inside-prop "keyroles") ; check naming conventions
(contains-list-of SEGMENTS_ACCOUNT :inside-prop "segments")
))

(entity SEGMENTS_ACCOUNT
(fields 
id :id
name
)
(relate(needs ACCOUNT prop:"id")))


(entity ACCOUNT_KEY_ROLE
(fields 
index :id :index
vatally_user:<="vitallyUser"
key_role:<="keyRole")
(relate(needs ACCOUNT prop:"id")))


(entity USER
(source (http/get :url "/resources/users")
(extract-path "results"))
(fields
id :id 
created_at:<="createdAt"
updated_at:<="updatedAt"
external_id:<="externalId"
name:<="name"
email:<="email"
avatar:<="avatar"
first_known:<="firstKnown"
last_seen_timestamp:<="lastSeenTimestamp"
last_inbound_message_timestamp:<="lastInboundMessageTimestamp"
last_outbound_message_timestamp:<="lastOutboundMessageTimestamp"
nps_last_score:<="npsLastScore"
nps_last_feedback:<="npsLastFeedback"
nps_last_responded_at:<="npsLastRespondedAt"
unsubscribed_from_conversations:<="unsubscribedFromConversations"
deactivated_at:<="deactivatedAt"
)
(dynamic-fields
(flatten-fields
(fields 
id
created_at:<="createdAt"
label
path
type
options)
:from "traits" ))
(relate
(contains-list-of ACCOUNT_ID :inside-prop "accountIds" :as "account_id" )  ; this is coming as list of array strings.
(contains-list-of ORGANIZATION_ID :inside-prop "organizationIds" :as "organization_id")
(contains-list-of SEGMENT_USER :inside-prop "segments")
))


(entity SEGMENT_USER
(fields 
id :id
name
)
(relate(needs USER prop:"id")))

(entity ACCOUNT_ID
(fields
account_id :id )
(relate (needs USER prop:"id")))

(entity ORGANIZATION_ID 
(fields 
organization_id :id
)
(relate(needs USER prop:"id")))

(entity ADMIN
(source 
(http/get :url"/resources/admins/search")
(extract-path ""))
(fields
id :id
name:<="name"
email:<="email"
license_status:<="licenseStatus")
)

(entity CONVERSATION
(source
(http/get :url "/resources/conversations")
(extract-path "results"))
(fields 
id :id
external_id :<="externalId"
subject
name 
source
accoutns  (json)
users (json)
admins (json)))


(entity MESSAGE
(source 
(http/get :url "/resources/messages/{CONVERSATION.id}"))
(fields 
id :id
type
external_id:<="externalId"
timestamp
message
)
(dynamic-fields 
(flatten-fields 
(fields 
type_from :<="type"
id_from :id :<="id"
)
:from "from"
))
(relate
(contains-list-of TOCONV :inside-prop "to")
(contains-list-of TOCC :inside-prop "cc")
(contains-list-of TOBCC :inside-prop "bcc")
))

(entity TOBCC 
(fields 
bcc_type:<="type"
bcc_id :id :<="id"
)
(relate 
(needs MESSAGE :prop "id")))

(entity TOCC 
(fields 
cc_type:<="type"
cc_id :id :<="id"
)
(relate
(needs MESSAGE :prop "id")))

(entity TO_CONV
(fields 
 totype:<="type"
 toid :id :<="id")
 (relate 
 (needs MESSAGE :prop "id")
))

(entity NOTES 
(source(
(http/get :url "/resources/notes")
(extract-path "results"))
)
(fields 
external_id :id :<="externalId"
subject
note
note_date :<="noteDate"
autor_id :<="authorId"
))
(entity NOTESACCOUNT
(source 
(http/get :url "resources/accounts/{ACCOUNT.id}/notes")
(extract-path "results")
)
(fields 
id :id
external_id  :<="externalId"
created_at:<="createdAt"
updated_at:<="updatedAt"
)
(relate
(linkes-to NOTES.external_id :prop "external_id")))

(entity PROJECT
(source 
(http/get :url "/resources/projects")
(extract-path("results"))
)
(fields 
index :id :index
 name 
 created_at:<="createdAt"
 owned_by_vitally_user_id:<=" ownedByVitallyUserId"       
 account_id:<=" accountId"
 project_status_id:<=" projectStatusId"
 target_start_date:<=" targetStartDate"
 duration_in_days:<="durationInDays"
))

(entity TASK 
(source
(http/get :url "resources/tasks")
(extract-path "results")
(query-params "archived" "false")
)
(fields 
external_id :id :<="externalId"
name
created_at:<="createdAt"
 updated_at:<=" updatedAt"
 created_by_id:<=" createdById"
 assigned_to_id:<=" assignedToId"
 completed_by_id:<=" completedById"
 due_date:<=" dueDate"
 completed_at:<=" completedAt"
 
))
(entity TASK_ACC 
(source )
(fields 
description)
(relate
(links-to TASK.external_id :prop "externalId")))


(entity NPS_RESPONSE 
(source 
(http/get :url "resources/npsResponses")
(extract-path "results"))
(fields 
id :id
external_id:<="externalId"
user_id:<="userId"
score 
feedback 
responded_at:<="respondedAt"
))

(entity traits 
(api-docs-url "https://docs.vitally.io/pushing-data-to-vitally/rest-api/custom-traits")
(source 
(http/get :url"resources/customFields/notes")
(extract-path "")
)
(fields 
index :id :index
label
type
path
created_at:<="createdAt"
)
)

(entity Project_Templates
(api-docs-url "https://docs.vitally.io/pushing-data-to-vitally/rest-api/project-templates")
(source
 (http/get :url "resources/projectTemplates")
 (extract-path "results")
 )
 (fields
 id :id
 created_at:<=" createdAt"
 updated_at:<=" updatedAt"
 project_name:<=" projectName"
 project_category_id:<="projectCategoryId"
))