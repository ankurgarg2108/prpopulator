package com.aurea.faster.prpopulator.service.impl;

import static org.springframework.util.StringUtils.isEmpty;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.aurea.faster.prpopulator.dto.PullRequestDTO;
import com.aurea.faster.prpopulator.service.GoogleSheetsService;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GoogleSheetsServiceImpl implements GoogleSheetsService {

	static final String SPREADSHEET_HEADER_CANT_BE_NULL_OR_EMPTY = "Can not read the google spreadsheet, header was not properly filled, should not be empty";
	static final String UNKNOWN_HEADER_COLUMN_FORMAT_MESSAGE = "Can not read the google spreadsheet, header was not properly filled, verify the value(%s)";
	private static final String USER = "commitReverterUser";
	private static final String ACCESS_TYPE_OFFLINE = "offline";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final String TOKENS_DIRECTORY_PATH = "tokens";
	private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
	private static final String APPLICATION_NAME = "PR Populator Application";
	@Value("${pr-populator.credentials-file-path:/credentials.json}")
	private String credentialsFilePath;
	@Value("${pr-populator.google-config.spreadsheetPRSheet.name:}")
	private String prSheetName;
	@Value("${pr-populator.google-config.spreadsheetPRSheet.id:}")
	private String prSheetId;
	@Value("${pr-populator.google-config.spreadsheetPRSheet.data-range}")
	private String dataRange;
	@Value("${pr-populator.google-config.spreadsheetPRSheet.prIdColumnName}")
	private String prIdColumnName;
	@Value("${pr-populator.google-config.spreadsheetPRSheet.prUrlColumnName}")
	private String prUrlColumnName;
	@Value("${pr-populator.google-config.spreadsheetPRSheet.jiraIdColumnName}")
	private String jiraIdColumnName;
	@Value("${pr-populator.google-config.spreadsheetPRSheet.productColumnName}")
	private String productColumnName;
	@Value("${pr-populator.google-config.spreadsheetPRSheet.quarterColumnName}")
	private String quarterColumnName;
	@Value("${pr-populator.google-config.spreadsheetPRSheet.yearColumnName}")
	private String yearColumnName;
	@Value("${pr-populator.google-config.spreadsheetPRSheet.maxEmptySequencialRows:10}")
	private int maxEmptySequencialRows;

	@Value("${pr-populator.google-config.spreadsheetProductToRepoSheet.name:}")
	private String prdRepoSheetName;
	@Value("${pr-populator.google-config.spreadsheetProductToRepoSheet.id:}")
	private String prdRepoSheetId;
	@Value("${pr-populator.google-config.spreadsheetProductToRepoSheet.data-range}")
	private String prdRepoSheetDataRange;
	@Value("${pr-populator.google-config.spreadsheetProductToRepoSheet.productColumnName}")
	private String prdRepoSheetProductColumnName;
	@Value("${pr-populator.google-config.spreadsheetProductToRepoSheet.repositoryUrlColumnName}")
	private String prdRepoSheetRepoURLColumnName;
	private NetHttpTransport netHttpTransport;

	@Override
	public Map<String, Set<String>> readProductToURLSheet() {
		LOGGER.debug("Started the process related to reading sheet RepoToProductMapping");
		Map<String, Set<String>> productRepoMap;
		try {
			Sheets service = new Sheets.Builder(getNetHttpTransport(), JSON_FACTORY, authorizeUser())
					.setApplicationName(APPLICATION_NAME).build();
			ValueRange valueRangeRetrieved = service.spreadsheets().values().get(prdRepoSheetId, prdRepoSheetDataRange)
					.execute();
			productRepoMap = valueRangeToRepoProductMap(valueRangeRetrieved);
			LOGGER.debug("Map for product to repo mapping {}", productRepoMap);
			return productRepoMap;
		} catch (IOException | GeneralSecurityException e) {
			LOGGER.error("Error while listing the pull requests from Google Spread Sheet", e);
			throw new IllegalStateException("Error for listing the pull requests from Google Spread Sheet.");
		}

	}

	private Map<String, Set<String>> valueRangeToRepoProductMap(ValueRange valueRange) {
		Map<String, Set<String>> map = new HashMap<>();
		if (Objects.nonNull(valueRange)) {
			final List<List<Object>> values = valueRange.getValues();
			if (values != null && !values.isEmpty()) {
				List<Object> headerRow = values.remove(0);
				Map<String, Integer> headerDefinitionMap = buildHeaderDefinitionForMappingSheet(headerRow);
				int emptySequencialRowCounter = 0;
				for (int i = 0; i < values.size() && emptySequencialRowCounter < maxEmptySequencialRows; i++) {
					List<Object> row = values.get(i);
					if (!row.isEmpty() && row.size() == 2 && !isEmpty(row.get(0)) && !isEmpty(row.get(1))) {
						String product = row.get(headerDefinitionMap.get(prdRepoSheetProductColumnName)).toString();
						String repoUrl = row.get(headerDefinitionMap.get(prdRepoSheetRepoURLColumnName)).toString();
						Set<String> productsForRepo = map.get(repoUrl);
						if (null == productsForRepo)
							productsForRepo = new HashSet<>();
						productsForRepo.add(product);
						map.put(repoUrl, productsForRepo);
						emptySequencialRowCounter = 0;
						LOGGER.debug("Row {} read.", i);
					} else {
						emptySequencialRowCounter++;
						LOGGER.debug("Row ignored, the columns are not properly filled, emptySequencialRowCounter: {}",
								emptySequencialRowCounter);
					}
				}
			}
		}
		return map;

	}

	@Override
	public List<PullRequestDTO> listPullRequests(Integer quarter, Integer year) {
		LOGGER.debug("Started the process related to listing the pull requests from Google spreadsheetPRSheet");
		List<PullRequestDTO> resultListPullRequest;
		try {
			Sheets service = new Sheets.Builder(getNetHttpTransport(), JSON_FACTORY, authorizeUser())
					.setApplicationName(APPLICATION_NAME).build();
			ValueRange valueRangeRetrieved = service.spreadsheets().values().get(prSheetId, dataRange).execute();
			List<PullRequestDTO> listPullRequest = valueRangeToListPullRequestDTO(valueRangeRetrieved);
			resultListPullRequest = filterListPullRequest(listPullRequest, quarter, year);
		} catch (IOException | GeneralSecurityException e) {
			LOGGER.error("Error while listing the pull requests from Google Spread Sheet", e);
			throw new IllegalStateException("Error for listing the pull requests from Google Spread Sheet.");
		}
		resultListPullRequest = new ArrayList<>(new HashSet<>(resultListPullRequest));
		return resultListPullRequest;
	}

	private List<PullRequestDTO> filterListPullRequest(List<PullRequestDTO> listPullRequest, Integer quarter,
			Integer year) {
		return listPullRequest.stream().filter(pr -> quarter.equals(pr.getQuarter()) && year.equals(pr.getYear()))
				.collect(Collectors.toList());
	}

	private List<PullRequestDTO> valueRangeToListPullRequestDTO(ValueRange valueRange) {
		List<PullRequestDTO> resultListPullRequest = new ArrayList<>();
		if (Objects.nonNull(valueRange)) {
			final List<List<Object>> values = valueRange.getValues();
			if (values != null && !values.isEmpty()) {
				List<Object> headerRow = values.remove(0);
				Map<String, Integer> headerDefinitionMap = buildHeaderDefinition(headerRow);
				resultListPullRequest = new ArrayList<>(values.size());
				int emptySequencialRowCounter = 0;
				for (int i = 0; i < values.size() && emptySequencialRowCounter < maxEmptySequencialRows; i++) {
					List<Object> row = values.get(i);
					if (!row.isEmpty() && row.size() == 6 && !isEmpty(row.get(0)) && !isEmpty(row.get(1))
							&& !isEmpty(row.get(2)) && !isEmpty(row.get(3)) && !isEmpty(row.get(4))
							&& !isEmpty(row.get(5))) {
						PullRequestDTO pullRequestDTO = convertRowToPullRequestDTO(headerDefinitionMap, row);
						resultListPullRequest.add(pullRequestDTO);
						emptySequencialRowCounter = 0;
						LOGGER.debug("Pull Request: {} read.", pullRequestDTO.getPrId());
					} else {
						emptySequencialRowCounter++;
						LOGGER.debug("Row ignored, the columns are not properly filled, emptySequencialRowCounter: {}",
								emptySequencialRowCounter);
					}
				}
			}
		}
		return resultListPullRequest;
	}

	private PullRequestDTO convertRowToPullRequestDTO(Map<String, Integer> headerDefinitionMap, List<Object> row) {
		return new PullRequestDTO(Integer.valueOf(row.get(headerDefinitionMap.get(prIdColumnName)).toString()),
				row.get(headerDefinitionMap.get(prUrlColumnName)).toString(),
				row.get(headerDefinitionMap.get(jiraIdColumnName)).toString(),
				row.get(headerDefinitionMap.get(productColumnName)).toString(),
				Integer.valueOf(row.get(headerDefinitionMap.get(quarterColumnName)).toString()),
				Integer.valueOf(row.get(headerDefinitionMap.get(yearColumnName)).toString()));
	}

	private Map<String, Integer> buildHeaderDefinition(List<Object> headerRow) {
		Map<String, Integer> resultHeaderDefinitionMap = new HashMap<>();
		if (headerRow != null && !headerRow.isEmpty()) {
			int index = 0;
			for (Object headerColumn : headerRow) {
				if (headerColumn.toString().equalsIgnoreCase(prIdColumnName)) {
					resultHeaderDefinitionMap.put(prIdColumnName, index++);
				} else if (headerColumn.toString().equalsIgnoreCase(prUrlColumnName)) {
					resultHeaderDefinitionMap.put(prUrlColumnName, index++);
				} else if (headerColumn.toString().equalsIgnoreCase(jiraIdColumnName)) {
					resultHeaderDefinitionMap.put(jiraIdColumnName, index++);
				} else if (headerColumn.toString().equalsIgnoreCase(productColumnName)) {
					resultHeaderDefinitionMap.put(productColumnName, index++);
				} else if (headerColumn.toString().equalsIgnoreCase(quarterColumnName)) {
					resultHeaderDefinitionMap.put(quarterColumnName, index++);
				} else if (headerColumn.toString().equalsIgnoreCase(yearColumnName)) {
					resultHeaderDefinitionMap.put(yearColumnName, index++);
				} else {
					throw new IllegalArgumentException(
							String.format(UNKNOWN_HEADER_COLUMN_FORMAT_MESSAGE, headerColumn.toString()));
				}
			}
		} else {
			throw new IllegalArgumentException(SPREADSHEET_HEADER_CANT_BE_NULL_OR_EMPTY);
		}
		return resultHeaderDefinitionMap;
	}

	private Map<String, Integer> buildHeaderDefinitionForMappingSheet(List<Object> headerRow) {
		Map<String, Integer> resultHeaderDefinitionMap = new HashMap<>();
		if (headerRow != null && !headerRow.isEmpty()) {
			int index = 0;
			for (Object headerColumn : headerRow) {
				if (headerColumn.toString().equalsIgnoreCase(prdRepoSheetProductColumnName)) {
					resultHeaderDefinitionMap.put(prdRepoSheetProductColumnName, index++);
				} else if (headerColumn.toString().equalsIgnoreCase(prdRepoSheetRepoURLColumnName)) {
					resultHeaderDefinitionMap.put(prdRepoSheetRepoURLColumnName, index++);
				} else {
					throw new IllegalArgumentException(
							String.format(UNKNOWN_HEADER_COLUMN_FORMAT_MESSAGE, headerColumn.toString()));
				}
			}
		} else {
			throw new IllegalArgumentException(SPREADSHEET_HEADER_CANT_BE_NULL_OR_EMPTY);
		}
		return resultHeaderDefinitionMap;
	}

	private Credential authorizeUser() throws IOException, GeneralSecurityException {
		GoogleClientSecrets clientSecrets = loadClientSecrets();
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(getNetHttpTransport(), JSON_FACTORY,
				clientSecrets, SCOPES).setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
						.setAccessType(ACCESS_TYPE_OFFLINE).build();
		return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize(USER);
	}

	private NetHttpTransport getNetHttpTransport() throws GeneralSecurityException, IOException {
		if (netHttpTransport == null) {
			this.netHttpTransport = GoogleNetHttpTransport.newTrustedTransport();
		}
		return this.netHttpTransport;
	}

	private GoogleClientSecrets loadClientSecrets() throws IOException {
		InputStream inputStream = GoogleSheetsServiceImpl.class.getResourceAsStream(credentialsFilePath);
		return GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(inputStream));
	}

	@Override
	public int writePullRequestDataToSheet(Set<PullRequestDTO> list) {
		// generate cellvalues
		if (!list.isEmpty()) {
			List<List<Object>> values = list.stream().map(pr -> new ArrayList<Object>(Arrays.asList(pr.getPrId(),
					pr.getUrl(), pr.getJiraId(), pr.getProduct(), pr.getQuarter(), pr.getYear())))
					.collect(Collectors.toList());
			LOGGER.debug("Cell values that will be written {}", values);
			try {
				Sheets service = new Sheets.Builder(getNetHttpTransport(), JSON_FACTORY, authorizeUser())
						.setApplicationName(APPLICATION_NAME).build();
				ValueRange valueRangeRetrieved = service.spreadsheets().values().get(prSheetId, dataRange).execute();
				ValueRange body = new ValueRange().setValues(values);
				LOGGER.debug("Sheet Range that will be written to {}",
						prSheetName + "!A" + (valueRangeRetrieved.getValues().size() + 1));
				UpdateValuesResponse result = service.spreadsheets().values()
						.update(prSheetId, prSheetName + "!A" + (valueRangeRetrieved.getValues().size() + 1), body)
						.setValueInputOption("RAW").execute();
				LOGGER.debug("Number of cells written to Google Sheet {}", result.getUpdatedCells());
				return result.getUpdatedRows();
			} catch (IOException | GeneralSecurityException e) {
				LOGGER.error("Error while writing pull request to Google Spread Sheet", e);
				throw new IllegalStateException("Error while writing pull request to Google Spread Sheet.");
			}
		}
		return 0;

	}

}
