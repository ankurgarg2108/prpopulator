package com.aurea.faster.prpopulator.processors;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.aurea.faster.prpopulator.dto.JIRATicketInfo;
import com.aurea.faster.prpopulator.dto.PullRequestDTO;
import com.aurea.faster.prpopulator.dto.PullRequestInfoDTO;
import com.aurea.faster.prpopulator.dto.RepoInfoDTO;
import com.aurea.faster.prpopulator.dto.TeamInfoDTO;

public class BaseTest {

	private static final String PRODUCT = "Smartleads";
	public static final String LIST_PRS_RESPONSE = "[\r\n" + "    {\r\n"
			+ "        \"url\": \"https://api.github.com/repos/trilogy-group/aurea-aes-edi/pulls/1620\",\r\n"
			+ "        \"id\": 238422172,\r\n" + "        \"node_id\": \"MDExOlB1bGxSZXF1ZXN0MjM4NDIyMTcy\",\r\n"
			+ "        \"html_url\": \"https://github.com/trilogy-group/aurea-aes-edi/pull/1620\",\r\n"
			+ "        \"number\": 1620,\r\n" + "        \"state\": \"open\",\r\n" + "        \"locked\": false,\r\n"
			+ "        \"title\": \"CC-205007: HUT for Long Methods :: projects/Java/ecp-core/src/main/java/com/ecpower/recv/plugin/RecvWebSite.java\",\r\n"
			+ "        \"body\": \"CC-205007: HUT for Long Methods :: projects/Java/ecp-core/src/main/java/com/ecpower/recv/plugin/RecvWebSite.java\",\r\n"
			+ "        \"created_at\": \"2018-12-13T15:06:15Z\",\r\n"
			+ "        \"updated_at\": \"2018-12-13T16:00:35Z\",\r\n" + "        \"closed_at\": null,\r\n"
			+ "        \"merged_at\": \"2018-12-13T16:00:35Z\"\r\n" + "    }\r\n" + "]";

	public static String LIST_TEAM_RESPONSE = "[\r\n" + "    {\r\n" + "        \"name\": \"aurea-crm-reviewers\",\r\n"
			+ "        \"id\": 2130233,\r\n" + "        \"node_id\": \"MDQ6VGVhbTIxMzAyMzM=\",\r\n"
			+ "        \"slug\": \"aurea-crm-reviewers\",\r\n"
			+ "        \"description\": \"ACRM eng.product team\",\r\n" + "        \"privacy\": \"closed\",\r\n"
			+ "        \"url\": \"https://api.github.com/teams/2130233\",\r\n"
			+ "        \"members_url\": \"https://api.github.com/teams/2130233/members{/member}\",\r\n"
			+ "        \"repositories_url\": \"https://api.github.com/teams/2130233/repos\",\r\n"
			+ "        \"permission\": \"pull\"\r\n" + "    }]";

	public static String LIST_REPOS_RESPONSE = "[\r\n" + "    {\r\n" + "        \"id\": 11820626,\r\n"
			+ "        \"node_id\": \"MDEwOlJlcG9zaXRvcnkxMTgyMDYyNg==\",\r\n"
			+ "        \"name\": \"Dnn.Evoq.Social\",\r\n"
			+ "        \"full_name\": \"trilogy-group/Dnn.Evoq.Social\",\r\n" + "        \"private\": true,\r\n"
			+ "        \"html_url\": \"https://github.com/trilogy-group/Dnn.Evoq.Social\",\r\n"
			+ "        \"pulls_url\": \"https://api.github.com/repos/trilogy-group/Dnn.Evoq.Social/pulls{/number}\"\r\n"
			+ "    },\r\n" + "    {\r\n" + "        \"id\": 12095100,\r\n"
			+ "        \"node_id\": \"MDEwOlJlcG9zaXRvcnkxMjA5NTEwMA==\",\r\n"
			+ "        \"name\": \"Dnn.Evoq.Content\",\r\n"
			+ "        \"full_name\": \"trilogy-group/Dnn.Evoq.Content\",\r\n"
			+ "        \"html_url\": \"https://github.com/trilogy-group/Dnn.Evoq.Content\",\r\n"
			+ "        \"pulls_url\": \"https://api.github.com/repos/trilogy-group/Dnn.Evoq.Content/pulls{/number}\"\r\n"
			+ "    }\r\n" + "]";

	public static String LIST_JIRA_RESPONSE = "{\r\n" + "    \"expand\": \"schema,names\",\r\n"
			+ "    \"startAt\": 0,\r\n" + "    \"maxResults\": 50,\r\n" + "    \"total\": 2,\r\n"
			+ "    \"issues\": [\r\n" + "        {\r\n"
			+ "            \"expand\": \"operations,versionedRepresentations,editmeta,changelog,renderedFields\",\r\n"
			+ "            \"id\": \"2491225\",\r\n"
			+ "            \"self\": \"https://jira.devfactory.com/rest/api/2/issue/2491225\",\r\n"
			+ "            \"key\": \"XOWS-2527\"\r\n" + "        },\r\n" + "        {\r\n"
			+ "            \"expand\": \"operations,versionedRepresentations,editmeta,changelog,renderedFields\",\r\n"
			+ "            \"id\": \"2680877\",\r\n"
			+ "            \"self\": \"https://jira.devfactory.com/rest/api/2/issue/2680877\",\r\n"
			+ "            \"key\": \"XOC-4330\"\r\n" + "        }\r\n" + "		]\r\n" + "		}";

	public static String LIST_JIRA_PR_REPSONSE = "{\r\n" + "    \"errors\": [],\r\n" + "    \"detail\": [\r\n"
			+ "        {\r\n" + "            \"pullRequests\": [\r\n" + "                {\r\n"
			+ "                    \"id\": \"#2674\",\r\n" + "                    \"status\": \"MERGED\",\r\n"
			+ "                    \"url\": \"https://github.com/trilogy-group/crossover-hiremanage-bandcamp/pull/2674\",\r\n"
			+ "                    \"lastUpdate\": \"2018-11-01T14:42:26.000+0000\"\r\n" + "                }\r\n"
			+ "            ]\r\n" + "        }\r\n" + "    ]\r\n" + "}";

	public List<PullRequestInfoDTO> getPullRequestInfoDTOList() {
		PullRequestInfoDTO dto = new PullRequestInfoDTO();
		dto.setTitle("SL-99889");
		dto.setFirstComment("SL-99889");
		dto.setNumber("100");
		dto.setCreatedAt(Instant.now().toString());
		PullRequestInfoDTO dto1 = new PullRequestInfoDTO();
		dto1.setTitle("SL-4567");
		dto1.setFirstComment("SL2-22331");
		dto1.setNumber("1000");
		dto.setCreatedAt(Instant.now().toString());
		List<PullRequestInfoDTO> list = new ArrayList<>();
		list.add(dto);
		list.add(dto1);
		return list;
	}

	public List<RepoInfoDTO> getRepoInfoDTO() {
		RepoInfoDTO dto = new RepoInfoDTO();
		dto.setId("id");
		dto.setRepoName("Unmapped");
		dto.setPullRequestURL("PRURL");
		dto.setRepoUrl("RepoURL");
		List<RepoInfoDTO> list = new ArrayList<>();
		list.add(dto);
		return list;
	}

	public Optional<TeamInfoDTO> getTeamInfoDTO() {
		TeamInfoDTO teamInfo = new TeamInfoDTO();
		teamInfo.setId("Id");
		teamInfo.setTeamName("TeamName");
		teamInfo.setFetchReposURL("RepoFetchURl");
		return Optional.of(teamInfo);
	}

	public List<JIRATicketInfo> setupListJIRATicketInfo() {
		List<JIRATicketInfo> listPullRequestDTO = new ArrayList<>();
		listPullRequestDTO.add(new JIRATicketInfo("SL-99889", "1", new ArrayList<>(), false, new ArrayList<>()));
		listPullRequestDTO.add(new JIRATicketInfo("SL2-22331", "2", new ArrayList<>(), false, new ArrayList<>()));
		listPullRequestDTO.add(new JIRATicketInfo("SL-99882", "3", new ArrayList<>(), false, new ArrayList<>()));
		return listPullRequestDTO;
	}

	public Map<String, Set<String>> getRepoProductMap() {
		Map<String, Set<String>> map = new HashMap<String, Set<String>>();
		Set<String> products = new HashSet<>();
		products.add("SL");
		map.put("SLRepo", products);
		return map;
	}

	public List<PullRequestDTO> setupListPullRequestDTO() {
		List<PullRequestDTO> listPullRequestDTO = new ArrayList<>();
		listPullRequestDTO.add(new PullRequestDTO(1, "http://pull-request.url", "SL-99888", PRODUCT, 3, 2018));
		listPullRequestDTO.add(new PullRequestDTO(2, "http://pull-request-2.url", "SL-22331", PRODUCT, 3, 2018));
		listPullRequestDTO.add(new PullRequestDTO(3, "http://pull-request-3.url", "SL-99887", PRODUCT, 2, 2018));
		listPullRequestDTO.add(new PullRequestDTO(4, "http://pull-request-4.url", "SL-22334", PRODUCT, 2, 2018));
		listPullRequestDTO.add(new PullRequestDTO(5, "http://pull-request-5.url", "SL-99882", PRODUCT, 3, 2017));
		listPullRequestDTO.add(new PullRequestDTO(6, "http://pull-request-6.url", "SL-22311", "ProductA", 1, 2017));
		listPullRequestDTO.get(0).setJiraId("SL-99888");
		return listPullRequestDTO;
	}
}
