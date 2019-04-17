# auth

# setup

Assumes a db in postgres named `auth`.

# oauth hydra server setup
[Setup spring as resource server with remote token services](https://stackoverflow.com/questions/45714067/configure-spring-security-with-hydra-oauth-2-0)

hydra docs
- [Guide: Integrating with (existing) User Management](https://www.ory.sh/docs/hydra/oauth2)
- [Reference implementation for user login and consent flow](https://github.com/ory/hydra-login-consent-node)
- [REST API docs](https://www.ory.sh/docs/hydra/sdk/api)

## Admin operations
#### Create a client
https://www.ory.sh/docs/hydra/sdk/api#create-an-oauth-20-client
```
curl -X POST \
  http://localhost:4445/clients \
  -H 'Content-Type: application/json' \
  -d '{
	"client_id": "my-client-credentials-test",
	"client_secret": "my-secret",
	"grant_types": ["client_credentials"],
	"token_endpoint_auth_method": "client_secret_post"
}'
```

Test the client by getting an access token with client_credentials
```
curl -X POST \                                                                                                                                                                    master ✱ ◼
  http://localhost:4444/oauth2/token \
  -F grant_type=client_credentials \
  -F client_id=my-client-credentials-test \
  -F client_secret=my-secret | less
```

Introspect the token
```
TOKEN='i1HOSC1cn3iWH6camZIDh12uDsrTBDzTL_iKHrtjmHA.6UBrzuqiM1k4PNLLjxMD-EjigOWHyMenZ0z65J2UHM0' curl -X POST \
  http://localhost:4445/oauth2/introspect \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d "token=$TOKEN"
```

### an example login/consent flow
Create your client on hydra
```
curl -X POST \
  http://localhost:4445/clients \
  -H 'Content-Type: application/json' \
  -d '{
	"client_id": "my-implicit-client",
	"client_secret": "my-secret",
	"grant_types": ["implicit"],
	"response_types": ["token"],
	"redirect_uris": ["http://localhost:3000/callback"],
	"token_endpoint_auth_method": "client_secret_post"
}'
```

Go to the oauth2/auth endpoint to start the flow
e.g.: 
localhost:4444/oauth2/auth?client_id=my-implicit-client&response_type=token&scope=offline&state=blahblahblah

### Questions
- When is the skip property in login request and consent request set to true? Why is the subject in those requests initially empty?