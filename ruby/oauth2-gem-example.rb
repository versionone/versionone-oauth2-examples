

def read_secrets()
  rawdata = JSON.parse( IO.read("client_secrets.json") )
  return rawdata["installed"] if rawdata.has_key? "installed"
  return rawdata["web"] if rawdata.has_key? "web"
  raise "Please make sure client_secrets.json file is present."
end


require 'oauth2'
require 'JSON'

callback_url = "urn:ietf:wg:oauth:2.0:oob"

registration = read_secrets()

client = OAuth2::Client.new(
  registration['client_id'],
  registration['client_secret'],
  :site => registration["server_base_uri"],
  :authorize_url => registration["auth_uri"],
  :token_url => registration["token_uri"]
  )

requested_scopes = "apiv1"

authorization_grant_url = client.auth_code.authorize_url(
  :redirect_uri => callback_url,
  :scope => requested_scopes
  )

puts "Please visit \n\n#{authorization_grant_url}\n\n and paste the authorization code from that page: "
auth_code = $stdin.read().strip
puts "Paste received: >>#{auth_code}<<"

token = client.auth_code.get_token(auth_code, :redirect_uri => callback_url)

api_url = registration["server_base_uri"] + "/rest-1.oauth.v1/Data/Scope"

begin
  response = token.get(api_url, :params => { "sel" => 'Name' })
  data = response.body
  puts data
rescue Error => er
  if er.response.status == 401
    token.refresh!
  response = token.get(api_url, :params => { "sel" => 'Name' })
    data = response.body
    puts data
  else
    raise
  end
end


