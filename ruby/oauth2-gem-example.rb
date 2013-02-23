
  def simple_oauth
    require 'oauth2'
    site_url = "http://localhost/VersionOne.Web/"
    registration_url = site_url + "oauth.mvc/register"
    callback_url = "urn:ietf:wg:oauth:2.0:oob"
    registration = HTTParty.post(registration_url, :body => {
      client_name: "rubyclient " + DateTime.now.to_s,
      redirect_uri: callback_url,
      client_type: "Public"
      })
    client = OAuth2::Client.new(
      registration['client_id'],
      registration['client_secret'],
      :site => site_url,
      :authorize_url => registration["server_auth_url"],
      :token_url => registration["server_token_url"]
      )
    requested_scopes = "apiv1"
    authorization_grant_url = @client.auth_code.authorize_url(
      :redirect_uri => callback_url,
      :scope => requested_scopes
      )
    puts "Please visit #{authorization_grant_url} and paste the authorization code from that page: "
    auth_code = gets.strip
    token = client.auth_code.get_token(auth_code, :redirect_uri => callback_url)
    api_url = site_url + "rest-1.oauth.v1/Data/Scope"
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
  end

