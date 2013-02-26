using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Text;
using Newtonsoft.Json;

namespace YamlClient
{

	class JsonClient
	{
		private readonly string _url;
		private readonly string _ticket;
		private WebClient _client;

		public JsonClient(string url, string ticket)
		{
			_url = url;
			_ticket = ticket;
			_client = new WebClient();
			_client.Headers["Cookie"] =
				".V1.Ticket.VersionOne.Web=" + ticket;
		}

		public List<List<dynamic>> GetResultSets(string querybody)
		{
			var body = Encoding.UTF8.GetString(_client.UploadData(_url, Encoding.UTF8.GetBytes(querybody)));
			var allsets = JsonConvert.DeserializeObject<List<List<dynamic>>>(body);
			return allsets;
		}


	}

	class Program
	{

		static void Main(string[] args)
		{
			var url = "http://localhost/VersionOne.Web/query.v1";

			var query = @"
from: Timebox
where:
  Name: Sprint 4
  Schedule.Name: Call Center Schedule
select:
  - Name
  - EndDate
  - BeginDate

---

from: Workitem

asof: All

where:
  AssetType: $typesToSum
  Timebox.Name: $iterationName

select:
  - ChangeDate
  - Name
  - AssetType
  - ToDo
  - from: Timebox
    select:
      - Name
      - Workitems:Test.ToDo.@Sum
      - Workitems:Task.ToDo.@Sum
sort:
  - ChangeDate

with:
  $typesToSum: Task,Test
  $scheduleName: Call Center Schedule
  $projectName: Call Center
  $iterationName: Sprint 4
";

			var client = new JsonClient(url, "HFZlcnNpb25PbmUuV2ViLkF1dGhlbnRpY2F0b3IUAAAABWFkbWluIKTDjq7hzwj/Pzf0dSjKKxC6xUr3zkFTpIb2zE6oHm2u");

			var results = client.GetResultSets(query).ToArray();
			var timeboxFound = results[0].First();
			var workitemsFound = results[1];
			var x = from workitem in workitemsFound
			        where
				        (DateTime)workitem["ChangeDate"] >= (DateTime)timeboxFound["BeginDate"]
						&& (DateTime)workitem["ChangeDate"] <= (DateTime)timeboxFound["EndDate"]
			        group workitem by ((DateTime)workitem["ChangeDate"]).Date
			        into workitemsByDay
			        from workitem in workitemsByDay
					let timebox = ((IEnumerable<dynamic>)workitem["Timebox"]).First()
						select "Sum: " + (int?)timebox["Workitems:Test.ToDo.@Sum"] + " " + (int?)timebox["Workitems:Task.ToDo.@Sum"];
			Console.WriteLine(JsonConvert.SerializeObject(x, Formatting.Indented));
			Console.ReadLine();
		}
	}
}
