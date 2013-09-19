using System;
using System.Collections.Generic;
using System.IO;
using System.Net;
using System.Text;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using OAuth2Client;

namespace YamlClient
{
    public class Program
    {
        private static void Main()
        {
            IStorage credentials = new Storage.JsonFileStorage(
                "../../client_secrets.json", "../../stored_credentials.json");
            const string scopes = "query-api-1.0 apiv1";
            const string url = "https://www14.v1host.com/v1sdktesting/query.v1";

            var client = new JsonClient(credentials, url, scopes);

            const string queryBody = @"
from: Scope
select:
    - Name
    - Workitems.@Count
    - Workitems:PrimaryWorkitem.@Count
    - Workitems:PrimaryWorkitem[Estimate>'0'].@Count
    - Workitems:PrimaryWorkitem[Estimate='0'].@Count
    - Workitems:PrimaryWorkitem[Estimate>'0'].Estimate.@Sum
    - from: Workitems:PrimaryWorkitem[Estimate>'0']
      select:
        - Name
        - Estimate
";

            var resultSets = client.GetResultSets(queryBody).ToArray();

            foreach (var result in resultSets[0]) // Rember that query.v1 returns a resultSet of resultSets!
            {
                Console.WriteLine(result["Name"]);
                Console.WriteLine("Total # of workitems: " + result["Workitems.@Count"]);
                Console.WriteLine("Total # of Primary workitems: " + result["Workitems:PrimaryWorkitem.@Count"]);
                Console.WriteLine("Total # of Estimated Primary workitems: " +
                                  result["Workitems:PrimaryWorkitem[Estimate>'0'].@Count"]);
                Console.WriteLine("Total # of Unestimated Primary workitems: " +
                                  result["Workitems:PrimaryWorkitem[Estimate='0'].@Count"]);
                Console.WriteLine("Sum of all Estimated Primary workitems: " +
                                  result["Workitems:PrimaryWorkitem[Estimate>'0'].Estimate.@Sum"]);
                foreach (var estimatedWorkitem in result["Workitems:PrimaryWorkitem[Estimate>'0']"])
                {
                    Console.WriteLine(estimatedWorkitem["Name"] + " : " + estimatedWorkitem["Estimate"]);
                }
                Console.WriteLine("\n");
            }

            Console.Write("Press any key to exit...");
            Console.ReadLine();
        }
    }

    public class JsonClient
	{
	    private readonly string _scopes;
		private readonly Uri _url;
		private readonly WebClient _client;
	    private readonly IStorage _storedCredentials;

		public JsonClient(IStorage storedCredentials, string url, string scopes)
		{
		    _scopes = scopes;
			_url = new Uri(url);
		    _storedCredentials = storedCredentials;
			_client = new WebClient { Encoding = Encoding.UTF8 };
		}

		public List<List<JObject>> GetResultSets(string queryBody)
		{
			var resultbody = _client.UploadStringOAuth2(_storedCredentials, 
                _scopes, _url.ToString(), queryBody);
			return JsonConvert.DeserializeObject<List<List<JObject>>>(resultbody);
		}
	}

    public static class WebClientExtensions
    {
        public static string UploadStringOAuth2(this WebClient client, 
            IStorage storage, string scopes, string path, string queryBody)
        {
            var creds = storage.GetCredentials();
            client.AddBearer(creds);
            try
            {
                return client.UploadString(path, queryBody);
            }
            catch (WebException ex)
            {
                if (ex.Status == WebExceptionStatus.ProtocolError)
                {
                    if (((HttpWebResponse)ex.Response).StatusCode != HttpStatusCode.Unauthorized)
                        throw;
                    var secrets = storage.GetSecrets();
                    var authclient = new AuthClient(secrets, scopes, null, null);
                    var newcreds = authclient.refreshAuthCode(creds);
                    var storedcreds = storage.StoreCredentials(newcreds);
                    client.AddBearer(storedcreds);
                    return client.UploadString(path, queryBody);
                }
                throw;
            }
        }
    }
}
