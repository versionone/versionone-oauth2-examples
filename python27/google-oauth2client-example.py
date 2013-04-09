import urllib2, base64, time, httplib2, sys, os, logging, json
from logging import debug, info
from pprint import pprint as pp

import oauth2client
import oauth2client.clientsecrets
import oauth2client.tools
from oauth2client.file import Storage
from oauth2client.client import flow_from_clientsecrets 

logging.basicConfig(level=logging.DEBUG)

import gflags
import sys

FLAGS = gflags.FLAGS
try:
  argv = FLAGS(sys.argv)  # parse flags
except gflags.FlagsError, e:
  print '%s\\nUsage: %s ARGS\\n%s' % (e, sys.argv[0], FLAGS)
  sys.exit(1)

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

info("Trying to get creds from file")
storage = Storage('stored_credentials.json')

credentials = storage.get()
if not credentials:
  credentials = oauth2client.tools.run(flow, storage, httpclient)  

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


