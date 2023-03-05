package com.wolterskluwer.osatutorial;

import com.wolterskluwer.osa.common.odata.api.Credentials;
import com.wolterskluwer.osa.common.odata.api.SecurityToken;
import com.wolterskluwer.osa.identity.odata.api.Authenticate;
import com.wolterskluwer.osa.identity.odata.api.AuthenticateResponse;
import com.wolterskluwer.osa.identity.odata.client.IdentityODataClient;
import com.wolterskluwer.osa.odata.commons.core.MediaStreamWrapper;
import com.wolterskluwer.osa.odata.commons.core.ODataVersion;
import com.wolterskluwer.osa.odata.commons.core.client.Query;
import com.wolterskluwer.osa.odata.commons.core.client.QueryBuilder;
import com.wolterskluwer.osa.research.odata.api.*;
import com.wolterskluwer.osa.research.odata.client.ResearchODataClient;
import com.wolterskluwer.osa.resource.odata.api.ContentSubject;
import com.wolterskluwer.osa.resource.odata.api.Document;
import com.wolterskluwer.osa.resource.odata.api.DocumentStream;
import com.wolterskluwer.osa.resource.odata.api.StreamDocument;
import com.wolterskluwer.osa.resource.odata.client.ResourceODataClient;
import lombok.extern.slf4j.Slf4j;
import org.opensaml.xmlsec.signature.Q;
import org.springframework.context.support.GenericXmlApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
public class App {
    public static void main(String[] args) {
        GenericXmlApplicationContext context = new GenericXmlApplicationContext("context.xml");

        IdentityODataClient identityClient = context.getAutowireCapableBeanFactory().getBean(IdentityODataClient.class);
        ResearchODataClient researchClient = context.getAutowireCapableBeanFactory().getBean(ResearchODataClient.class);
        ResourceODataClient resourceClient = context.getAutowireCapableBeanFactory().getBean(ResourceODataClient.class);

        Authenticate authenticateRequest = new Authenticate();
        Credentials credentials = new Credentials();
        credentials.setLoginId("testuser");
        credentials.setPassword("password");
        authenticateRequest.setCredentials(credentials);
        authenticateRequest.setCreateSession(true);
        AuthenticateResponse authenticateResponse = null;
        try {
            authenticateResponse = identityClient.getAuthenticationOperationSet().authenticate(authenticateRequest);
            if(authenticateResponse.getSecurityToken() != null) log.info("Got securitytoken: "+authenticateResponse.getSecurityToken().getContent());
            else throw new RuntimeException("No SecurityToken received");
        } catch (Exception e) {
            log.error("Login failed with exception ", e);
        }

        assert authenticateResponse != null;
        researchClient.getClientContext().getRequestInfo().setSecurityToken(authenticateResponse.getSecurityToken());
        ExecuteSearch executeSearchRequest = new ExecuteSearch();
        executeSearchRequest.setQuery("homaging");
        SearchResult result = null;
        try {
            Search searchResponse= researchClient.getSearchOperationSet().executeSearch(executeSearchRequest);
            QueryBuilder.Builder query = new QueryBuilder.Builder(ODataVersion.V4).expand(Search.Result, SearchResult.Items);
            Search searchResult = researchClient.getSearchEntitySet().getEntity(searchResponse.getId(), query.build());
            result = searchResult.getResult();
            int totalHits = searchResult.getResult().getTotalHits().intValue(); //it is becoming 0
            log.info("There are "+totalHits+" total hits for this search");
        } catch (Exception e) {
            log.error("Search failed with exception ", e);
        }
        assert result != null;
        DocumentSearchResultItem item = (DocumentSearchResultItem) result.getItems().asList().get(0); //that is why this line is throwing NPE
        log.info("User selected search item with title "+item.getTitle()+" and documentId "+item.getDocumentId());

        Document document = null;
        try {
            document = resourceClient.getDocumentEntitySet().getEntity(item.getDocumentId());
            log.info("Retrieved document with id "+document.getId()+ " Summary: "+document.getSummary());
        } catch (Exception e) {
            log.error("getDocument failed with exception ", e);
        }

        try {
            InputStream inputStream = resourceClient.getDocumentEntitySet().getStream(item.getId()).getStream();
            InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            final char[] buffer = new char[100];
            final StringBuilder out = new StringBuilder();
            for(;;) {
                int rsz = reader.read(buffer, 0, buffer.length);
                if (rsz < 0)
                    break;
                out.append(buffer, 0, rsz);
            }
            log.info("Retieved document content: \n"+out.toString());
        } catch (Exception e) {
            log.error("getDocumentAsStream failed with exception ", e);
        }

    }
}