package com.aurea.faster.prpopulator.service.impl;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.aurea.faster.prpopulator.dto.PullRequestDTO;
import com.aurea.faster.prpopulator.service.GoogleSheetsService;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.sheets.v4.model.ValueRange;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GoogleSheetServiceTest {

	private static final String JIRA_ID = "SL-28332";
	private static final String PRODUCT = "Smartleads";
	private static final String FILTER_LIST_PULL_REQUEST = "filterListPullRequest";
	private static final String URL = "https://github.com/trilogy-group/ta-smartleads-lms-mct/pull/2371";
	private static final String YEAR_2018 = "2018";
	private static final String BUILD_HEADER_DEFINITION = "buildHeaderDefinition";
	@Autowired
	private GoogleSheetsService googleSheetService;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void givenValueRangeWhenValueRangeToListPullRequestDTOThenVerifyBehaviour()
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		// Arrange
		ValueRange valueRange = setupValueRangeMultipleRows();
		// Act
		Method privateMethod = googleSheetService.getClass().getDeclaredMethod("valueRangeToListPullRequestDTO",
				valueRange.getClass());
		privateMethod.setAccessible(true);
		@SuppressWarnings("unchecked")
		List<PullRequestDTO> result = (List<PullRequestDTO>) privateMethod.invoke(googleSheetService, valueRange);
		// Assert
		assertEquals(6, result.size());
	}

	@Test
	public void givenValueRangeWithEmptyRowsWhenValueRangeToListPullRequestDTOThenVerifyBehaviour()
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		// Arrange
		ValueRange valueRange = setupValueRangeMultipleRows();
		IntStream.range(1, 20).forEach(val -> valueRange.getValues().add(createRow("", "", "", "", "", "")));
		valueRange.getValues().add(createRow("100", URL, "SL-99999", PRODUCT, "3", YEAR_2018));
		// Act
		Method privateMethod = googleSheetService.getClass().getDeclaredMethod("valueRangeToListPullRequestDTO",
				valueRange.getClass());
		privateMethod.setAccessible(true);
		@SuppressWarnings("unchecked")
		List<PullRequestDTO> result = (List<PullRequestDTO>) privateMethod.invoke(googleSheetService, valueRange);
		// Assert
		assertEquals(6, result.size());
	}

	@Test
	public void givenGoogleSheetServiceWhenBuildHeaderDefinitionForMappingSheetInvokedWithNullThenExceptionShouldbeThrown()
			throws Throwable {
		// Arrange
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(GoogleSheetsServiceImpl.SPREADSHEET_HEADER_CANT_BE_NULL_OR_EMPTY);
		// Act
		try {
			Method privateMethod = googleSheetService.getClass()
					.getDeclaredMethod("buildHeaderDefinitionForMappingSheet", List.class);
			privateMethod.setAccessible(true);
			privateMethod.invoke(googleSheetService, new Object[] { null });
		} catch (Exception ex) {
			// workaround for reflection
			throw ((InvocationTargetException) ex).getTargetException();
		}
	}

	@Test
	public void givenGoogleSheetServiceWhenBuildHeaderDefinitionInvokedWithNullThenExceptionShouldbeThrown()
			throws Throwable {
		// Arrange
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(GoogleSheetsServiceImpl.SPREADSHEET_HEADER_CANT_BE_NULL_OR_EMPTY);
		// Act
		try {
			Method privateMethod = googleSheetService.getClass().getDeclaredMethod(BUILD_HEADER_DEFINITION, List.class);
			privateMethod.setAccessible(true);
			privateMethod.invoke(googleSheetService, new Object[] { null });
		} catch (Exception ex) {
			// workaround for reflection
			throw ((InvocationTargetException) ex).getTargetException();
		}
	}

	@Test
	public void givenGoogleSheetServiceWhenBuildHeaderDefinitionInvokedWithEmptyThenExceptionShouldbeThrown()
			throws Throwable {
		// Arrange
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(GoogleSheetsServiceImpl.SPREADSHEET_HEADER_CANT_BE_NULL_OR_EMPTY);
		// Act
		try {
			Method privateMethod = googleSheetService.getClass().getDeclaredMethod(BUILD_HEADER_DEFINITION, List.class);
			privateMethod.setAccessible(true);
			privateMethod.invoke(googleSheetService, new ArrayList<>());
		} catch (Exception ex) {
			// workaround for reflection
			throw ((InvocationTargetException) ex).getTargetException();
		}
	}

	@Test
	public void givenGoogleSheetServiceWhenBuildHeaderDefinitionInvokedWithWrongColumnThenExceptionShouldbeThrown()
			throws Throwable {
		// Arrange
		exception.expect(IllegalArgumentException.class);
		final String unknownColumn = "UnknownColumn";
		exception.expectMessage(format(GoogleSheetsServiceImpl.UNKNOWN_HEADER_COLUMN_FORMAT_MESSAGE, unknownColumn));
		// Act
		try {
			Method privateMethod = googleSheetService.getClass().getDeclaredMethod(BUILD_HEADER_DEFINITION, List.class);
			privateMethod.setAccessible(true);
			privateMethod.invoke(googleSheetService, new ArrayList<>(singletonList(unknownColumn)));
		} catch (Exception ex) {
			// workaround for reflection
			throw ((InvocationTargetException) ex).getTargetException();
		}
	}

	@Test
	public void givenValueRangeWhenValueRangeToRepoProductMapThenVerifyBehaviour()
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		// Arrange
		ValueRange valueRange = setupValueRangeMultipleRowsForProdRepoSheet();
		// Act
		Method privateMethod = googleSheetService.getClass().getDeclaredMethod("valueRangeToRepoProductMap",
				valueRange.getClass());
		privateMethod.setAccessible(true);
		@SuppressWarnings("unchecked")
		Map<String, Set<String>> result = (Map<String, Set<String>>) privateMethod.invoke(googleSheetService,
				valueRange);
		// Assert
		assertEquals(2, result.size());
	}

	@Test
	public void givenGoogleSheetServiceWhenNetHttpTransportThenNewInstanceShouldBeReturned()
			throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		// Arrange
		// Act
		Method privateMethod = googleSheetService.getClass().getDeclaredMethod("getNetHttpTransport");
		privateMethod.setAccessible(true);
		@SuppressWarnings("unchecked")
		NetHttpTransport transport = (NetHttpTransport) privateMethod.invoke(googleSheetService);
		// Assert
		assertNotNull(transport);
	}

	@Test
	public void givenListPRDTOWhenFilterPullRequestSpecifyingQuarterThenVerifyBehaviour()
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		// Given
		Integer quarter = 2;
		Integer year = 2018;
		List<PullRequestDTO> listPullRequestDTO = setupListPullRequestDTO();
		// When
		Method privateMethod = googleSheetService.getClass().getDeclaredMethod(FILTER_LIST_PULL_REQUEST, List.class,
				Integer.class, Integer.class);
		privateMethod.setAccessible(true);
		@SuppressWarnings("unchecked")
		List<PullRequestDTO> result = (List<PullRequestDTO>) privateMethod.invoke(googleSheetService,
				listPullRequestDTO, quarter, year);
		// Then
		assertEquals(2, result.size());
	}

	@Test
	public void givenListPRDTOWhenFilterPullRequestSpecifyingYearThenVerifyBehaviour()
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		// Given
		Integer quarter = 2;
		Integer year = 2018;
		List<PullRequestDTO> listPullRequestDTO = setupListPullRequestDTO();
		// When
		Method privateMethod = googleSheetService.getClass().getDeclaredMethod(FILTER_LIST_PULL_REQUEST, List.class,
				Integer.class, Integer.class);
		privateMethod.setAccessible(true);
		@SuppressWarnings("unchecked")
		List<PullRequestDTO> result = (List<PullRequestDTO>) privateMethod.invoke(googleSheetService,
				listPullRequestDTO, quarter, year);
		// Then
		assertEquals(2, result.size());
	}

	private ValueRange setupValueRangeMultipleRowsForProdRepoSheet() {
		List<List<Object>> values = new ArrayList<>(
				asList(createRow("Product", "Repository URL"), createRow("A", "B"), createRow("C", "D")));
		ValueRange valueRange = new ValueRange();
		valueRange.setValues(values);
		return valueRange;
	}

	private List<PullRequestDTO> setupListPullRequestDTO() {
		List<PullRequestDTO> listPullRequestDTO = new ArrayList<>();
		listPullRequestDTO.add(new PullRequestDTO(1, "http://pull-request.url", "SL-99888", PRODUCT, 3, 2018));
		listPullRequestDTO.add(new PullRequestDTO(2, "http://pull-request-2.url", "SL-22331", PRODUCT, 3, 2018));
		listPullRequestDTO.add(new PullRequestDTO(3, "http://pull-request-3.url", "SL-99887", PRODUCT, 2, 2018));
		listPullRequestDTO.add(new PullRequestDTO(4, "http://pull-request-4.url", "SL-22334", PRODUCT, 2, 2018));
		listPullRequestDTO.add(new PullRequestDTO(5, "http://pull-request-5.url", "SL-99882", PRODUCT, 3, 2017));
		listPullRequestDTO.add(new PullRequestDTO(6, "http://pull-request-6.url", "SL-22311", "ProductA", 1, 2017));
		return listPullRequestDTO;
	}

	private ValueRange setupValueRangeMultipleRows() {
		List<List<Object>> values = new ArrayList<>(
				asList(createRow("PR ID", "PR URL", "JIRA ID", "Product", "Quarter", "Year"),
						createRow("1", URL, JIRA_ID, PRODUCT, "3", YEAR_2018),
						createRow("2", URL, "SL-28331", PRODUCT, "3", YEAR_2018),
						createRow("3", URL, "SL-28330", PRODUCT, "3", YEAR_2018),
						createRow("4", URL, "SL-28329", PRODUCT, "2", YEAR_2018),
						createRow("5", URL, "SL-28328", PRODUCT, "2", YEAR_2018),
						createRow("6", URL, "SL-28327", PRODUCT, "2", "2017")));
		ValueRange valueRange = new ValueRange();
		valueRange.setValues(values);
		return valueRange;
	}

	private List<Object> createRow(String prId, String prUrl, String jiraId, String product, String quarter,
			String year) {
		return asList(prId, prUrl, jiraId, product, quarter, year);
	}

	private List<Object> createRow(String product, String repoUrl) {
		return asList(product, repoUrl);
	}

}
