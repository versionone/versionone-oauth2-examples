import urllib2, base64, time, httplib2, sys, os, logging, json
from logging import debug, info
from pprint import pprint as pp

logging.basicConfig(level=logging.DEBUG)


# Bring in Google oauth2 library functions

import oauth2client
import oauth2client.clientsecrets
import oauth2client.tools
from oauth2client.file import Storage
from oauth2client.client import flow_from_clientsecrets 


# Handle command-line args for google oauth2 library

import gflags
FLAGS = gflags.FLAGS
try:
  argv = FLAGS(sys.argv)  # parse flags
except gflags.FlagsError, e:
  print '%s\\nUsage: %s ARGS\\n%s' % (e, sys.argv[0], FLAGS)
  sys.exit(1)


secrets_file = 'client_secrets.json'
scopes_requested = 'query-api-1.0 test:grant_15s'
default_server_url = "http://localhost/VersionOne.Web"
api_endpoint_path = "/query.v1"

try:
  flow = flow_from_clientsecrets(
      secrets_file,
      scope = scopes_requested ,
      redirect_uri = 'urn:ietf:wg:oauth:2.0:oob'
      )

  raw_secrets_data = json.load(open(secrets_file,"r"))
  secrets_data = raw_secrets_data.get("installed", raw_secrets_data.get("web", None))

except oauth2client.clientsecrets.InvalidClientSecretsError:
  print "Please download the client_secrets.json file from the VersionOne permitted applications page"
  print "and save it in the current directory (%s)" %(os.getcwd(),)
  sys.exit(1)

v1_api_endpoint = secrets_data.get(u"server_base_uri", default_server_url) + api_endpoint_path

info("Using data URL " + v1_api_endpoint)

# if using a proxy, you can configure the http client with proxy details here.
# or use the HTTP_PROXY environment variables, which will be read by default.

httpclient = httplib2.Http(disable_ssl_certificate_validation=True)

info("Trying to get creds from file")

storage = Storage('stored_credentials.json')
credentials = storage.get()
if not credentials:
  credentials = oauth2client.tools.run(flow, storage, httpclient)  


credentials.authorize(httpclient)

info("Added authorization to http client object")


querybody = """
from: AssetType
select:
  - Name
  - from: AttributeDefinitions
    select:
      - Name
      - IsRequired
      - AttributeType
"""
info("Trying request to " + v1_api_endpoint)

try:
  headers, body = httpclient.request(v1_api_endpoint, body=querybody, method="POST")
  info("Response headers: " + str(headers))
  info("Response body: " + body)
except oauth2client.client.AccessTokenRefreshError as error:
  info("Problem with stored credentials.  Please run this program again and follow the steps to re-establish credentials.")
  sys.exit(2)


