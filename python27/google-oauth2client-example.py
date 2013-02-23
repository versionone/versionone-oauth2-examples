import urllib2, base64, time

from oauth2client.client import OAuth2WebServerFlow


q = """
from: AssetType
select:
  - Name
  - from: AttributeDefinitions
    select:
      - Name
      - AttributeType
"""

qs = '---'.join([q] * 10)

flow = OAuth2WebServerFlow(auth_uri="http://localhost/VersionOne.Web/OAuth2.mvc/Auth",
                           token_uri="http://localhost/VersionOne.Web/oauth.mvc/token",
                           client_id='f3a061f8-9096-4be9-82ce-66466e3ca5e1',
                           client_secret='czBG2HLZoiBZjmVA',
                           scope='yaml-query-api-0.5.0',
                           redirect_uri='urn:ietf:wg:oauth:2.0:oob')

auth_uri = flow.step1_get_authorize_url(redirect_uri="urn:ietf:wg:oauth:2.0:oob")
print "Go to " + auth_uri + " and paste the code found there:"
code = raw_input().strip()
credentials = flow.step2_exchange(code)

request = urllib2.Request("http://localhost/VersionOne.Web/query.v1", data=qs)
credentials.apply(request.headers)

t0 = time.time(); result = urllib2.urlopen(request).read(); t1=time.time(); print t1-t0



