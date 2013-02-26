using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Text;
using System.Threading.Tasks;
using Newtonsoft.Json;

namespace YamlClient
{

	class JsonClient
	{
		private readonly Uri _url;
		private readonly string _ticket;
		private WebClient _client;

		public JsonClient(string url, string ticket)
		{
			_url = new Uri(url);
			_ticket = ticket;
			_client = new WebClient { Encoding = Encoding.UTF8 };
			_client.Headers["Cookie"] = ".V1.Ticket.VersionOne.Web=" + ticket;
		}

		public List<List<dynamic>> GetResultSets(string querybody)
		{
			var resultbody = _client.UploadString(_url, "SEARCH", querybody);
			return JsonConvert.DeserializeObject<List<List<dynamic>>>(resultbody);
		}
	}

	class Program
	{
		static void Main(string[] args)
		{
			var url = "http://localhost/VersionOne.Web/query.v1";
			var authTicket = "HFZlcnNpb25PbmUuV2ViLkF1dGhlbnRpY2F0b3IUAAAABWFkbWluIKTDjq7hzwj/Pzf0dSjKKxC6xUr3zkFTpIb2zE6oHm2u";

			var client = new JsonClient(url, authTicket);

			var results = client.GetResultSets(QueryBody).ToArray();
			var timeboxFound = results[0].First();
			var workitemsFound = results[1];

			var iterationSums =
				from workitem in workitemsFound
				let changeDate = (DateTime) workitem.ChangeDate
				where
					changeDate >= (DateTime) timeboxFound.BeginDate &&
					changeDate <= (DateTime) timeboxFound.EndDate
				group workitem by changeDate.Date
				into workitemsByDay
				select new {
					Day = workitemsByDay.Key,
					TaskSum = workitemsByDay.Sum( workitem => {
						var timeboxes = (IEnumerable<dynamic>) workitem.Timebox;
						var timebox = timeboxes.First();
						var tasks = (int?) timebox["Workitems:Task.ToDo.@Sum"];
						var tests = (int?) timebox["Workitems:Test.ToDo.@Sum"];
						return tasks + tests;
					})
				};

			Console.WriteLine(JsonConvert.SerializeObject(iterationSums, Formatting.Indented));
			Console.ReadLine();
		}

		public const string QueryBody = @"
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

	}
}
