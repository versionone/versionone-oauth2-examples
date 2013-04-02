import urllib2, base64, time, httplib2, sys, os, logging
from logging import debug, info
from pprint import pprint as pp

import oauth2client
import oauth2client.clientsecrets
from oauth2client.file import Storage
from oauth2client.client import OAuth2WebServerFlow
from oauth2client.client import flow_from_clientsecrets



logging.basicConfig(level=logging.DEBUG)

v1_instance_url = "http://localhost/VersionOne.Web/"
v1_api_endpoint = v1_instance_url + "rest-1.oauth.v1"


try:
  flow = flow_from_clientsecrets(
      'client_secrets.json',
      scope='apiv1 test:grant_15s',
      redirect_uri='urn:ietf:wg:oauth:2.0:oob'
      )
except oauth2client.clientsecrets.InvalidClientSecretsError:
  print "Please download the client_secrets.json file from the VersionOne permitted applications page"
  print "and save it in the current directory (%s)" %(os.getcwd(),)
  print
  sys.exit(1)

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
except oauth2client.client.AccessTokenRefreshError as error:
  storage.delete()
  info("Problem with stored credentials.  Please run this program again and follow the steps to re-establish credentials.")
  sys.exit(2)

info("Response headers: " + str(headers))

info("Response body: " + body)

