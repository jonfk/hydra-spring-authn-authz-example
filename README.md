# auth

# setup

Assumes a db in postgres named `auth`.

# oauth hydra server setup
https://stackoverflow.com/questions/45714067/configure-spring-security-with-hydra-oauth-2-0

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

Test the client
```
curl -X POST \                                                                                                                                                                    master ✱ ◼
  http://localhost:4444/oauth2/token \
  -F grant_type=client_credentials \
  -F client_id=my-client-credentials-test \
  -F client_secret=my-secret | less
```
