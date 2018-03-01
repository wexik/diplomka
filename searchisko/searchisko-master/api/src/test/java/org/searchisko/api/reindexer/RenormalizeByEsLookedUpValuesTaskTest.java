/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.searchisko.api.reindexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.indices.IndexMissingException;
import org.elasticsearch.search.SearchHit;
import org.jboss.elasticsearch.tools.content.InvalidDataException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.searchisko.api.ContentObjectFields;
import org.searchisko.api.rest.exception.PreprocessorInvalidDataException;
import org.searchisko.api.service.ProviderService;
import org.searchisko.api.service.ProviderServiceTest;
import org.searchisko.api.service.SearchClientService;
import org.searchisko.api.tasker.TaskExecutionContext;
import org.searchisko.api.testtools.ESRealClientTestBase;
import org.searchisko.api.testtools.TestUtils;

/**
 * Unit test for {@link RenormalizeByEsLookedUpValuesTask}
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class RenormalizeByEsLookedUpValuesTaskTest extends ESRealClientTestBase {

	String contributorCode = "project 1";
	String contributorCode2 = "project 2";
	String sysContentType = "tt";
	String indexName = "myindex";
	String typeName = "mytype";

	@SuppressWarnings("unchecked")
	@Test
	public void performHitProcessing_PreprocessorInvalidDataException() throws Exception {
		RenormalizeByEsLookedUpValuesTask tested = new RenormalizeByEsLookedUpValuesTask();
		tested.providerService = Mockito.mock(ProviderService.class);
		TaskExecutionContext context = Mockito.mock(TaskExecutionContext.class);
		tested.setExecutionContext("tid", context);

		List<Map<String, Object>> preprocessorsDef = new ArrayList<Map<String, Object>>();
		configProviderServiceMock(tested, preprocessorsDef);

		SearchHit hit = Mockito.mock(SearchHit.class);
		BulkRequestBuilder brb = Mockito.mock(BulkRequestBuilder.class);
		Client client = Mockito.mock(Client.class);

		Mockito.when(hit.getId()).thenReturn("hitid");
		Map<String, Object> content = new HashMap<String, Object>();
		content.put(ContentObjectFields.SYS_CONTENT_TYPE, sysContentType);
		Mockito.when(hit.getSource()).thenReturn(content);
		Mockito.when(
				tested.providerService.runPreprocessors(Mockito.eq(sysContentType), Mockito.anyList(), Mockito.anyMap()))
				.thenThrow(new PreprocessorInvalidDataException(new InvalidDataException("data error")));

		tested.performHitProcessing(client, brb, hit);

		Mockito.verify(context).writeTaskLog("tid",
				"ERROR: Data error from preprocessors execution so document hitid is skipped: data error");
		Mockito.verifyNoMoreInteractions(context, brb, client);

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void performTask_ok() throws Exception {

		try {
			RenormalizeByEsLookedUpValuesTask tested = new RenormalizeByEsLookedUpValuesTask();
			tested.searchClientService = Mockito.mock(SearchClientService.class);
			Mockito.when(tested.searchClientService.getClient()).thenReturn(prepareESClientForUnitTest("RenormalizeByEsLookedUpValuesTaskTest"));
			tested.lookupIndex = "sys_projects";
			tested.lookupType = "project";
			tested.lookupField = "type_specific_code.jbossorg_jira";

			tested.esValues = new String[] { contributorCode };
			tested.providerService = Mockito.mock(ProviderService.class);
			TaskExecutionContext contextMock = Mockito.mock(TaskExecutionContext.class);
			tested.setExecutionContext("tid", contextMock);
			List<Map<String, Object>> preprocessorsDef = new ArrayList<Map<String, Object>>();

			// case - run on nonexisting index
			try {
				indexDelete(indexName);
				configProviderServiceMock(tested, preprocessorsDef);
				tested.performTask();
				Assert.fail("IndexMissingException expected");
			} catch (IndexMissingException e) {
				Mockito.verify(tested.providerService, Mockito.times(0)).runPreprocessors(Mockito.eq(sysContentType),
						Mockito.eq(preprocessorsDef), Mockito.anyMap());
			}

			indexCreate(indexName);
			indexMappingCreate(indexName, typeName, "{ \"" + typeName
					+ "\" : {\"properties\": {\"sys_contributors\" : {\"type\" : \"string\", \"analyzer\" : \"keyword\"}}}}");
			// case - run on empty index
			{
				Mockito.reset(tested.providerService);
				configProviderServiceMock(tested, preprocessorsDef);
				tested.performTask();
				indexFlushAndRefresh(indexName);
				Mockito.verify(tested.providerService, Mockito.times(0)).runPreprocessors(Mockito.eq(sysContentType),
						Mockito.eq(preprocessorsDef), Mockito.anyMap());
				Mockito.verify(contextMock).writeTaskLog(Mockito.anyString(), Mockito.eq("Processed 0 documents."));
			}

			// case - run on non empty index, for one project
			{
				indexInsertDocument(indexName, typeName, "tt-1",
						"{\"id\" : \"tt1\", \"sys_contributors\" : [\"project 3\",\"project 1\"], \"sys_content_type\" : \"tt\"}");
				indexInsertDocument(indexName, typeName, "tt-2",
						"{\"id\" : \"tt2\", \"sys_contributors\" : \"project 1\", \"sys_content_type\" : \"tt\"}");
				indexInsertDocument(indexName, typeName, "tt-3",
						"{\"id\" : \"tt3\", \"sys_contributors\" : \"project 1\", \"sys_content_type\" : \"tt\"}");
				indexInsertDocument(indexName, typeName, "tt-4",
						"{\"id\" : \"tt4\", \"sys_contributors\" : \"project 2\", \"sys_content_type\" : \"tt\"}");
				indexInsertDocument(indexName, typeName, "tt-5",
						"{\"id\" : \"tt5\", \"sys_contributors\" : [\"project 1\",\"project 2\"], \"sys_content_type\" : \"tt\"}");
				indexInsertDocument(indexName, typeName, "tt-6",
						"{\"id\" : \"tt6\", \"sys_contributors\" : \"project 2\", \"sys_content_type\" : \"tt\"}");

				// next must be skipped due nonexisting type definition
				indexInsertDocument(indexName, typeName, "tt-7",
						"{\"id\" : \"tt7\", \"sys_contributors\" : \"project 1\", \"sys_content_type\" : \"tt2\"}");
				indexInsertDocument(indexName, typeName, "tt-8",
						"{\"id\" : \"tt8\", \"sys_contributors\" : \"project 3\", \"sys_content_type\" : \"tt\"}");
				indexFlushAndRefresh(indexName);

				Mockito.reset(tested.providerService, contextMock);
				configProviderServiceMock(tested, preprocessorsDef);

				Mockito.doAnswer(new Answer() {

					@Override
					public Object answer(InvocationOnMock invocation) throws Throwable {
						Map<String, Object> m = (Map<String, Object>) invocation.getArguments()[2];
						m.put("called", "yes");
						return null;
					}
				}).when(tested.providerService)
						.runPreprocessors(Mockito.eq(sysContentType), Mockito.anyList(), Mockito.anyMap());

				tested.performTask();

				indexFlushAndRefresh(indexName);
				// check preprocessors was called for all documents in index
				Assert.assertEquals("yes", indexGetDocument(indexName, typeName, "tt-1").get("called"));
				Assert.assertNotNull(indexGetDocument(indexName, typeName, "tt-2").get("called"));
				Assert.assertNotNull(indexGetDocument(indexName, typeName, "tt-3").get("called"));
				Assert.assertNull(indexGetDocument(indexName, typeName, "tt-4").get("called"));
				Assert.assertNotNull(indexGetDocument(indexName, typeName, "tt-5").get("called"));
				Assert.assertNull(indexGetDocument(indexName, typeName, "tt-6").get("called"));
				Assert.assertNull(indexGetDocument(indexName, typeName, "tt-7").get("called"));
				Assert.assertNull(indexGetDocument(indexName, typeName, "tt-8").get("called"));
				Mockito.verify(tested.providerService, Mockito.times(4)).runPreprocessors(Mockito.eq(sysContentType),
						Mockito.anyList(), Mockito.anyMap());
				Mockito.verify(contextMock).writeTaskLog(Mockito.anyString(),
						Mockito.eq("No type definition found for document id=tt-7 so is skipped"));
				Mockito.verify(contextMock).writeTaskLog(Mockito.anyString(), Mockito.eq("Processed 5 documents."));
			}

			// case - run on non empty index, for more projects
			{
				tested.esValues = new String[] { contributorCode, contributorCode2 };

				Mockito.reset(tested.providerService, contextMock);
				configProviderServiceMock(tested, preprocessorsDef);

				Mockito.doAnswer(new Answer() {

					@Override
					public Object answer(InvocationOnMock invocation) throws Throwable {
						Map<String, Object> m = (Map<String, Object>) invocation.getArguments()[2];
						m.put("called2", "yes");
						return null;
					}
				}).when(tested.providerService)
						.runPreprocessors(Mockito.eq(sysContentType), Mockito.anyList(), Mockito.anyMap());

				tested.performTask();

				indexFlushAndRefresh(indexName);
				// check preprocessors was called for all documents in index
				Assert.assertEquals("yes", indexGetDocument(indexName, typeName, "tt-1").get("called2"));
				Assert.assertNotNull(indexGetDocument(indexName, typeName, "tt-2").get("called2"));
				Assert.assertNotNull(indexGetDocument(indexName, typeName, "tt-3").get("called2"));
				Assert.assertNotNull(indexGetDocument(indexName, typeName, "tt-4").get("called2"));
				Assert.assertNotNull(indexGetDocument(indexName, typeName, "tt-5").get("called2"));
				Assert.assertNotNull(indexGetDocument(indexName, typeName, "tt-6").get("called2"));
				Assert.assertNull(indexGetDocument(indexName, typeName, "tt-7").get("called2"));
				Assert.assertNull(indexGetDocument(indexName, typeName, "tt-8").get("called2"));
				Mockito.verify(tested.providerService, Mockito.times(6)).runPreprocessors(Mockito.eq(sysContentType),
						Mockito.anyList(), Mockito.anyMap());
				Mockito.verify(contextMock).writeTaskLog(Mockito.anyString(),
						Mockito.eq("No type definition found for document id=tt-7 so is skipped"));
				Mockito.verify(contextMock).writeTaskLog(Mockito.anyString(), Mockito.eq("Processed 7 documents."));
			}

		} finally {
			finalizeESClientForUnitTest();
		}
	}

	@Test
	public void takeLookedUpEsFields() {
		RenormalizeByEsLookedUpValuesTask tested = new RenormalizeByEsLookedUpValuesTask();
		tested.searchClientService = Mockito.mock(SearchClientService.class);
		Mockito.when(tested.searchClientService.getClient()).thenReturn(Mockito.mock(Client.class));

		try {
			tested.takeLookedUpEsFields(null, "mytype");
			Assert.fail("NullPointerException expected");
		} catch (NullPointerException e) {

		}

		{
			Map<String, Object> typeDef = new HashMap<String, Object>();
			Set<String> ret = tested.takeLookedUpEsFields(ProviderServiceTest.createProviderContentTypeInfo(typeDef),
					"mytype");
			Assert.assertNotNull(ret);
			Assert.assertTrue(ret.isEmpty());
		}

		Map<String, Object> typeDef = TestUtils.loadJSONFromClasspathFile("/reindexer/typeDef_1.json");
		// case - lookup field not present in typeDef
		{
			tested.lookupIndex = "sys_projects";
			tested.lookupType = "project";
			tested.lookupField = "unknown";
			Set<String> ret = tested.takeLookedUpEsFields(ProviderServiceTest.createProviderContentTypeInfo(typeDef),
					"mytype");
			Assert.assertNotNull(ret);
			Assert.assertTrue(ret.isEmpty());
		}

		// case - nomatching lookupType
		{
			tested.lookupIndex = "sys_projects";
			tested.lookupType = "project2";
			tested.lookupField = "type_specific_code.jbossorg_jira";
			Set<String> ret = tested.takeLookedUpEsFields(ProviderServiceTest.createProviderContentTypeInfo(typeDef),
					"mytype");
			Assert.assertNotNull(ret);
			Assert.assertTrue(ret.isEmpty());
		}

		// case - one source field
		{
			tested.lookupIndex = "sys_projects";
			tested.lookupType = "project";
			tested.lookupField = "type_specific_code.jbossorg_jira";
			Set<String> ret = tested.takeLookedUpEsFields(ProviderServiceTest.createProviderContentTypeInfo(typeDef),
					"mytype");
			Assert.assertNotNull(ret);
			Assert.assertEquals(1, ret.size());
			Assert.assertTrue(ret.contains("project_key"));
		}

		// case - source bases used
		{
			tested.lookupIndex = "sys_contributors";
			tested.lookupType = "contributor";
			tested.lookupField = "email";
			Set<String> ret = tested.takeLookedUpEsFields(ProviderServiceTest.createProviderContentTypeInfo(typeDef),
					"mytype");
			Assert.assertNotNull(ret);
			Assert.assertEquals(4, ret.size());
			Assert.assertTrue(ret.contains("reporter.email_address"));
			Assert.assertTrue(ret.contains("assignee.email_address"));
			Assert.assertTrue(ret.contains("comments.comment_author.email_address"));
			Assert.assertTrue(ret.contains("comments.comment_updater.email_address"));
		}

		// case - source_value instead source_field
		{
			tested.lookupIndex = "sys_roles";
			tested.lookupType = "role";
			tested.lookupField = "type_specific_code.jbossorg_blog";
			Set<String> ret = tested.takeLookedUpEsFields(ProviderServiceTest.createProviderContentTypeInfo(typeDef),
					"mytype");
			Assert.assertNotNull(ret);
			Assert.assertTrue(ret.isEmpty());
		}

		// case - collect from more preprocessors
		typeDef = TestUtils.loadJSONFromClasspathFile("/reindexer/typeDef_2.json");
		{
			tested.lookupIndex = "sys_projects";
			tested.lookupType = "project";
			tested.lookupField = "type_specific_code.jbossorg_jira";
			Set<String> ret = tested.takeLookedUpEsFields(ProviderServiceTest.createProviderContentTypeInfo(typeDef),
					"mytype");
			Assert.assertNotNull(ret);
			Assert.assertEquals(5, ret.size());
			Assert.assertTrue(ret.contains("project_key"));
			Assert.assertTrue(ret.contains("reporter.email_address"));
			Assert.assertTrue(ret.contains("assignee.email_address"));
			Assert.assertTrue(ret.contains("comments.comment_author.email_address"));
			Assert.assertTrue(ret.contains("comments.comment_updater.email_address"));
		}
	}

	private void configProviderServiceMock(RenormalizeByEsLookedUpValuesTask tested,
			List<Map<String, Object>> preprocessorsDef) {
		Map<String, Object> typeDef = TestUtils.loadJSONFromClasspathFile("/reindexer/typeDef_performTaskTest.json");
		Mockito.when(tested.providerService.findContentType(sysContentType)).thenReturn(
				ProviderServiceTest.createProviderContentTypeInfo(typeDef));
		Mockito.when(tested.providerService.findContentType("tt2")).thenReturn(null);

		Map<String, Object> providerDef = new HashMap<String, Object>();
		Map<String, Object> typesDef = new HashMap<String, Object>();
		providerDef.put(ProviderService.TYPE, typesDef);

		typesDef.put(sysContentType, typeDef);
		List<Map<String, Object>> providers = new ArrayList<Map<String, Object>>();
		providers.add(providerDef);
		Mockito.when(tested.providerService.getAll()).thenReturn(providers);
	}

}
