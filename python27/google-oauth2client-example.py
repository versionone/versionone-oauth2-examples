import urllib2, base64, time, httplib2, sys, os, logging, json
from logging import debug, info
from pprint import pprint as pp

import oauth2client
import oauth2client.clientsecrets
from oauth2client.file import Storage
from oauth2client.client import OAuth2WebServerFlow
from oauth2client.client import flow_from_clientsecrets



logging.basicConfig(level=logging.DEBUG)



secrets_file = 'client_secrets.json'

try:
  flow = flow_from_clientsecrets(
      secrets_file,
      scope='apiv1 test:grant_15s',
      redirect_uri='urn:ietf:wg:oauth:2.0:oob'
      )

  raw_secrets_data = json.load(open(secrets_file,"r"))
  secrets_data = raw_secrets_data.get("installed", raw_secrets_data.get("web", None))
except oauth2client.clientsecrets.InvalidClientSecretsError:
  print "Please download the client_secrets.json file from the VersionOne permitted applications page"
  print "and save it in the current directory (%s)" %(os.getcwd(),)
  print
  sys.exit(1)


v1_api_endpoint = secrets_data.get(u"server_base_uri", "http://localhost/VersionOne.Web") + "/rest-1.oauth.v1"

print "Using data URL " + v1_api_endpoint

# if using a proxy, you can configure the http client with proxy details here.
# or use the HTTP_PROXY environment variables, which will be read by default.
httpclient = httplib2.Http()

def get_creds():
  info("creating oauth2 client instance, using pre-arranged client registration information")
  info("About to execute step 1: get authorization url to send user to, which contains metadata from the client registration")
  auth_uri = flow.step1_get_authorize_url()
  print
  print "********************************************************"
  print
  print "Please visit " + auth_uri
  print "You must accept the requested permissions, copy the code returned, and paste the code here:"
  print
  code = raw_input().strip()
  print
  print "Paste received. "
  print
  info("Now exchanging authorization code for an authorization and refresh token")
  credentials = flow.step2_exchange(code, httpclient)
  info("Received authorization and refresh token credentials from the authorization server:")
  return credentials


info("Trying to get creds from file")
storage = Storage('stored_credentials.json')
credentials = storage.get()

if not credentials:
  info("Failed to get creds from file.")
  credentials = get_creds()
  storage.put(credentials)

credentials.authorize(httpclient)

info("Added authorization to http client object")

def doRequest(url, authorized_client):
  info("Making data request to protected resource:")
  response = authorized_client.request(url)
  return response

api_query_url = v1_api_endpoint + "/Data/Scope/0"

info("doing request to " + api_query_url)
try:
  headers, body = doRequest(api_query_url, httpclient)
  info("Response headers: " + str(headers))
  info("Response body: " + body)
except oauth2client.client.AccessTokenRefreshError as error:
  storage.delete()
  info("Problem with stored credentials.  Please run this program again and follow the steps to re-establish credentials.")
  sys.exit(2)


