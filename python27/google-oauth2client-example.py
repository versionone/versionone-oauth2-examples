import urllib2, base64, time, httplib2, sys

from oauth2client.client import OAuth2WebServerFlow
import oauth2client
import oauth2client.clientsecrets

import logging
from logging import debug, info
logging.basicConfig(level=logging.DEBUG)

from pprint import pprint as pp

v1_instance_url = "http://localhost/VersionOne.Web/"
v1_api_endpoint = v1_instance_url + "rest-1.oauth.v1"


from oauth2client.client import flow_from_clientsecrets

try:
  flow = flow_from_clientsecrets(
    'client_secrets.json',
    scope='apiv1',
    redirect_uri='urn:ietf:wg:oauth:2.0:oob'
    )
except ZeroDivisionError:
  print "Please download the client-secrets.json file and save it in the current directory."
  sys.exit(1)

# if using a proxy, you can configure the http client with proxy details here.
# or use the HTTP_PROXY environment variables, which will be read by default.

httpclient = httplib2.Http()


debug("creating oauth2 client instance, using pre-arranged client registration information")


# # registration data is the result of manual client registration on the versionone server (which acts as an OAuth2 Authorization Server)
# registration = dict(
#   auth_uri="http://localhost/VersionOne.Web/OAuth2.mvc/Auth",
#   token_uri="http://localhost/VersionOne.Web/oauth.mvc/token",
#   client_id='f3a061f8-9096-4be9-82ce-66466e3ca5e1',
#   client_secret='czBG2HLZoiBZjmVA',
#   scope='yaml-query-api-0.5.0',
#   redirect_uri='urn:ietf:wg:oauth:2.0:oob',
#   )

# flow = OAuth2WebServerFlow(**registration)
# this_instance_endpoint_url = "urn:ietf:wg:oauth:2.0:oob"


info("About to execute step 1: get authorization url to send user to, which contains metadata from the client registration")
#pp(registration)

auth_uri = flow.step1_get_authorize_url()

info("About to execute step 1: get authorization url to send user to, which contains metadata from the client registration")

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
pp(credentials)

credentials.authorize(httpclient)

info("Added authorization to http client object")

def doRequestWithoutClient(url, credentials):
  request = urllib2.Request(url)
  info("Request headers modified to use access credentials:")
  credentials.apply(request)
  pp(request.headers)
  info("Making data request to protected resource:")
  body = urllib2.urlopen(request).read()
  info(body)
  return body

def doRequest(url, authorized_client):
  #request = urllib2.Request(url)
  #info("Request headers modified to use access credentials:")
  #credentials.apply(request)
  #pp(request.headers)
  info("Making data request to protected resource:")
  response = authorized_client.request(url)
  #body = urllib2.urlopen(request).read()
  return response

api_query_url = v1_api_endpoint + "/Data/Scope/0"

info("doing request to " + api_query_url)

headers, body = doRequest(api_query_url, httpclient)
info("Response headers: " + str(headers))

info("Response body: " + body)

