package com.icthh.xm.ms.entity.web.rest.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;

/**
 * Tests based on parsing algorithm in app/components/util/pagination-util.service.js
 *
 * @see PaginationUtil
 */
public class PaginationUtilUnitTest {

    @Test
    public void generatePaginationHttpHeadersTest() {
        String baseUrl = "/api/_search/example";
        List<String> content = new ArrayList<>();
        Page<String> page = new PageImpl<>(content,new PageRequest(6, 50),400L);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, baseUrl);
        List<String> strHeaders = headers.get(HttpHeaders.LINK);
        assertNotNull(strHeaders);
        assertTrue(strHeaders.size() == 1);
        String headerData = strHeaders.get(0);
        assertTrue(headerData.split(",").length == 4);
        String expectedData = "</api/_search/example?page=7&size=50>; rel=\"next\","
                + "</api/_search/example?page=5&size=50>; rel=\"prev\","
                + "</api/_search/example?page=7&size=50>; rel=\"last\","
                + "</api/_search/example?page=0&size=50>; rel=\"first\"";
        assertEquals(expectedData, headerData);
        List<String> xTotalCountHeaders = headers.get("X-Total-Count");
        assertTrue(xTotalCountHeaders.size() == 1);
        assertTrue(Long.valueOf(xTotalCountHeaders.get(0)).equals(400L));
    }

    @Test
    public void commaTest() {
        String baseUrl = "/api/_search/example";
        List<String> content = new ArrayList<>();
        Page<String> page = new PageImpl<>(content);
        String query = "Test1, test2";
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, baseUrl);
        List<String> strHeaders = headers.get(HttpHeaders.LINK);
        assertNotNull(strHeaders);
        assertTrue(strHeaders.size() == 1);
        String headerData = strHeaders.get(0);
        assertTrue(headerData.split(",").length == 2);
        String expectedData = "</api/_search/example?page=0&size=0&query=Test1%2C+test2>; rel=\"last\","
                + "</api/_search/example?page=0&size=0&query=Test1%2C+test2>; rel=\"first\"";
        assertEquals(expectedData, headerData);
        List<String> xTotalCountHeaders = headers.get("X-Total-Count");
        assertTrue(xTotalCountHeaders.size() == 1);
        assertTrue(Long.valueOf(xTotalCountHeaders.get(0)).equals(0L));
    }

    @Test
    public void commaTestWithTemplate() {
        String baseUrl = "/api/_search/example";
        List<String> content = new ArrayList<>();
        Page<String> page = new PageImpl<>(content);
        String template = "Test4";
        String[] templateParams = {"Test5", "Test6"};
        HttpHeaders headers = PaginationUtil.generateSearchWithTemplatePaginationHttpHeaders(template, templateParams, page, baseUrl);
        List<String> strHeaders = headers.get(HttpHeaders.LINK);
        assertNotNull(strHeaders);
        assertTrue(strHeaders.size() == 1);
        String headerData = strHeaders.get(0);
        assertTrue(headerData.split(",").length == 2);
        String expectedData = "</api/_search/example?page=0&size=0&template=Test4&templateParams=Test5%2CTest6>; rel=\"last\","
            + "</api/_search/example?page=0&size=0&template=Test4&templateParams=Test5%2CTest6>; rel=\"first\"";
        assertEquals(expectedData, headerData);
        List<String> xTotalCountHeaders = headers.get("X-Total-Count");
        assertTrue(xTotalCountHeaders.size() == 1);
        assertTrue(Long.valueOf(xTotalCountHeaders.get(0)).equals(0L));
    }

    @Test
    public void nullTest() {
        String baseUrl = "/api/_search/example";
        List<String> content = new ArrayList<>();
        Page<String> page = new PageImpl<>(content);
        String query = null;
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, baseUrl);
        List<String> strHeaders = headers.get(HttpHeaders.LINK);
        assertNotNull(strHeaders);
        assertTrue(strHeaders.size() == 1);
        String headerData = strHeaders.get(0);
        assertTrue(headerData.split(",").length == 2);
        String expectedData = "</api/_search/example?page=0&size=0&query=>; rel=\"last\","
            + "</api/_search/example?page=0&size=0&query=>; rel=\"first\"";
        assertEquals(expectedData, headerData);
        List<String> xTotalCountHeaders = headers.get("X-Total-Count");
        assertTrue(xTotalCountHeaders.size() == 1);
        assertTrue(Long.valueOf(xTotalCountHeaders.get(0)).equals(0L));
    }

    @Test
    public void nullTestWithTemplate() {
        String baseUrl = "/api/_search/example";
        List<String> content = new ArrayList<>();
        Page<String> page = new PageImpl<>(content);
        String template = null;
        String[] templateParams = null;
        HttpHeaders headers = PaginationUtil.generateSearchWithTemplatePaginationHttpHeaders(template, templateParams, page, baseUrl);
        List<String> strHeaders = headers.get(HttpHeaders.LINK);
        assertNotNull(strHeaders);
        assertTrue(strHeaders.size() == 1);
        String headerData = strHeaders.get(0);
        assertTrue(headerData.split(",").length == 2);
        String expectedData = "</api/_search/example?page=0&size=0&template=&templateParams=>; rel=\"last\","
            + "</api/_search/example?page=0&size=0&template=&templateParams=>; rel=\"first\"";
        assertEquals(expectedData, headerData);
        List<String> xTotalCountHeaders = headers.get("X-Total-Count");
        assertTrue(xTotalCountHeaders.size() == 1);
        assertTrue(Long.valueOf(xTotalCountHeaders.get(0)).equals(0L));
    }

    @Test
    public void multiplePagesTest() {
        String baseUrl = "/api/_search/example";
        List<String> content = new ArrayList<>();

        // Page 0
        Page<String> page = new PageImpl<>(content,new PageRequest(0, 50),400L);
        String query = "Test1, test2";
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, baseUrl);
        List<String> strHeaders = headers.get(HttpHeaders.LINK);
        assertNotNull(strHeaders);
        assertTrue(strHeaders.size() == 1);
        String headerData = strHeaders.get(0);
        assertTrue(headerData.split(",").length == 3);
        String expectedData = "</api/_search/example?page=1&size=50&query=Test1%2C+test2>; rel=\"next\","
                + "</api/_search/example?page=7&size=50&query=Test1%2C+test2>; rel=\"last\","
                + "</api/_search/example?page=0&size=50&query=Test1%2C+test2>; rel=\"first\"";
        assertEquals(expectedData, headerData);
        List<String> xTotalCountHeaders = headers.get("X-Total-Count");
        assertTrue(xTotalCountHeaders.size() == 1);
        assertTrue(Long.valueOf(xTotalCountHeaders.get(0)).equals(400L));

        // Page 1
        page = new PageImpl<>(content,new PageRequest(1, 50),400L);
        headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, baseUrl);
        strHeaders = headers.get(HttpHeaders.LINK);
        assertNotNull(strHeaders);
        assertTrue(strHeaders.size() == 1);
        headerData = strHeaders.get(0);
        assertTrue(headerData.split(",").length == 4);
        expectedData = "</api/_search/example?page=2&size=50&query=Test1%2C+test2>; rel=\"next\","
                + "</api/_search/example?page=0&size=50&query=Test1%2C+test2>; rel=\"prev\","
                + "</api/_search/example?page=7&size=50&query=Test1%2C+test2>; rel=\"last\","
                + "</api/_search/example?page=0&size=50&query=Test1%2C+test2>; rel=\"first\"";
        assertEquals(expectedData, headerData);
        xTotalCountHeaders = headers.get("X-Total-Count");
        assertTrue(xTotalCountHeaders.size() == 1);
        assertTrue(Long.valueOf(xTotalCountHeaders.get(0)).equals(400L));

        // Page 6
        page = new PageImpl<>(content,new PageRequest(6, 50),400L);
        headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, baseUrl);
        strHeaders = headers.get(HttpHeaders.LINK);
        assertNotNull(strHeaders);
        assertTrue(strHeaders.size() == 1);
        headerData = strHeaders.get(0);
        assertTrue(headerData.split(",").length == 4);
        expectedData = "</api/_search/example?page=7&size=50&query=Test1%2C+test2>; rel=\"next\","
                + "</api/_search/example?page=5&size=50&query=Test1%2C+test2>; rel=\"prev\","
                + "</api/_search/example?page=7&size=50&query=Test1%2C+test2>; rel=\"last\","
                + "</api/_search/example?page=0&size=50&query=Test1%2C+test2>; rel=\"first\"";
        assertEquals(expectedData, headerData);
        xTotalCountHeaders = headers.get("X-Total-Count");
        assertTrue(xTotalCountHeaders.size() == 1);
        assertTrue(Long.valueOf(xTotalCountHeaders.get(0)).equals(400L));

        // Page 7
        page = new PageImpl<>(content,new PageRequest(7, 50),400L);
        headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, baseUrl);
        strHeaders = headers.get(HttpHeaders.LINK);
        assertNotNull(strHeaders);
        assertTrue(strHeaders.size() == 1);
        headerData = strHeaders.get(0);
        assertTrue(headerData.split(",").length == 3);
        expectedData = "</api/_search/example?page=6&size=50&query=Test1%2C+test2>; rel=\"prev\","
                + "</api/_search/example?page=7&size=50&query=Test1%2C+test2>; rel=\"last\","
                + "</api/_search/example?page=0&size=50&query=Test1%2C+test2>; rel=\"first\"";
        assertEquals(expectedData, headerData);
    }

    @Test
    public void multiplePagesTestWithTemplate() {
        String baseUrl = "/api/_search/example";
        List<String> content = new ArrayList<>();

        // Page 0
        Page<String> page = new PageImpl<>(content,new PageRequest(0, 50),400L);
        String template = "Test4";
        String[] templateParams = {"Test5", "Test6"};
        HttpHeaders headers = PaginationUtil.generateSearchWithTemplatePaginationHttpHeaders(template, templateParams, page, baseUrl);
        List<String> strHeaders = headers.get(HttpHeaders.LINK);
        assertNotNull(strHeaders);
        assertTrue(strHeaders.size() == 1);
        String headerData = strHeaders.get(0);
        assertTrue(headerData.split(",").length == 3);
        String expectedData = "</api/_search/example?page=1&size=50&template=Test4&templateParams=Test5%2CTest6>; rel=\"next\","
            + "</api/_search/example?page=7&size=50&template=Test4&templateParams=Test5%2CTest6>; rel=\"last\","
            + "</api/_search/example?page=0&size=50&template=Test4&templateParams=Test5%2CTest6>; rel=\"first\"";
        assertEquals(expectedData, headerData);
        List<String> xTotalCountHeaders = headers.get("X-Total-Count");
        assertTrue(xTotalCountHeaders.size() == 1);
        assertTrue(Long.valueOf(xTotalCountHeaders.get(0)).equals(400L));

        // Page 1
        page = new PageImpl<>(content,new PageRequest(1, 50),400L);
        headers = PaginationUtil.generateSearchWithTemplatePaginationHttpHeaders(template, templateParams, page, baseUrl);
        strHeaders = headers.get(HttpHeaders.LINK);
        assertNotNull(strHeaders);
        assertTrue(strHeaders.size() == 1);
        headerData = strHeaders.get(0);
        assertTrue(headerData.split(",").length == 4);
        expectedData = "</api/_search/example?page=2&size=50&template=Test4&templateParams=Test5%2CTest6>; rel=\"next\","
            + "</api/_search/example?page=0&size=50&template=Test4&templateParams=Test5%2CTest6>; rel=\"prev\","
            + "</api/_search/example?page=7&size=50&template=Test4&templateParams=Test5%2CTest6>; rel=\"last\","
            + "</api/_search/example?page=0&size=50&template=Test4&templateParams=Test5%2CTest6>; rel=\"first\"";
        assertEquals(expectedData, headerData);
        xTotalCountHeaders = headers.get("X-Total-Count");
        assertTrue(xTotalCountHeaders.size() == 1);
        assertTrue(Long.valueOf(xTotalCountHeaders.get(0)).equals(400L));

        // Page 6
        page = new PageImpl<>(content,new PageRequest(6, 50),400L);
        headers = PaginationUtil.generateSearchWithTemplatePaginationHttpHeaders(template, templateParams, page, baseUrl);
        strHeaders = headers.get(HttpHeaders.LINK);
        assertNotNull(strHeaders);
        assertTrue(strHeaders.size() == 1);
        headerData = strHeaders.get(0);
        assertTrue(headerData.split(",").length == 4);
        expectedData = "</api/_search/example?page=7&size=50&template=Test4&templateParams=Test5%2CTest6>; rel=\"next\","
            + "</api/_search/example?page=5&size=50&template=Test4&templateParams=Test5%2CTest6>; rel=\"prev\","
            + "</api/_search/example?page=7&size=50&template=Test4&templateParams=Test5%2CTest6>; rel=\"last\","
            + "</api/_search/example?page=0&size=50&template=Test4&templateParams=Test5%2CTest6>; rel=\"first\"";
        assertEquals(expectedData, headerData);
        xTotalCountHeaders = headers.get("X-Total-Count");
        assertTrue(xTotalCountHeaders.size() == 1);
        assertTrue(Long.valueOf(xTotalCountHeaders.get(0)).equals(400L));

        // Page 7
        page = new PageImpl<>(content,new PageRequest(7, 50),400L);
        headers = PaginationUtil.generateSearchWithTemplatePaginationHttpHeaders(template, templateParams, page, baseUrl);
        strHeaders = headers.get(HttpHeaders.LINK);
        assertNotNull(strHeaders);
        assertTrue(strHeaders.size() == 1);
        headerData = strHeaders.get(0);
        assertTrue(headerData.split(",").length == 3);
        expectedData = "</api/_search/example?page=6&size=50&template=Test4&templateParams=Test5%2CTest6>; rel=\"prev\","
            + "</api/_search/example?page=7&size=50&template=Test4&templateParams=Test5%2CTest6>; rel=\"last\","
            + "</api/_search/example?page=0&size=50&template=Test4&templateParams=Test5%2CTest6>; rel=\"first\"";
        assertEquals(expectedData, headerData);
    }

    @Test
    public void greaterSemicolonTest() {
        String baseUrl = "/api/_search/example";
        List<String> content = new ArrayList<>();
        Page<String> page = new PageImpl<>(content);
        String query = "Test>;test";
        String template = "Test4>";
        String[] templateParams = {"Test5>", "Test6>"};
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, baseUrl);
        List<String> strHeaders = headers.get(HttpHeaders.LINK);
        assertNotNull(strHeaders);
        assertTrue(strHeaders.size() == 1);
        String headerData = strHeaders.get(0);
        assertTrue(headerData.split(",").length == 2);
        String[] linksData = headerData.split(",");
        assertTrue(linksData.length == 2);
        assertTrue(linksData[0].split(">;").length == 2);
        assertTrue(linksData[1].split(">;").length == 2);
        String expectedData = "</api/_search/example?page=0&size=0&query=Test%3E%3Btest>; rel=\"last\","
                + "</api/_search/example?page=0&size=0&query=Test%3E%3Btest>; rel=\"first\"";
        assertEquals(expectedData, headerData);
        List<String> xTotalCountHeaders = headers.get("X-Total-Count");
        assertTrue(xTotalCountHeaders.size() == 1);
        assertTrue(Long.valueOf(xTotalCountHeaders.get(0)).equals(0L));
    }

    @Test
    public void greaterSemicolonTestWithTemplate() {
        String baseUrl = "/api/_search/example";
        List<String> content = new ArrayList<>();
        Page<String> page = new PageImpl<>(content);
        String template = "Test4>";
        String[] templateParams = {"Test5>", "Test6>"};
        HttpHeaders headers = PaginationUtil.generateSearchWithTemplatePaginationHttpHeaders(template, templateParams, page, baseUrl);
        List<String> strHeaders = headers.get(HttpHeaders.LINK);
        assertNotNull(strHeaders);
        assertTrue(strHeaders.size() == 1);
        String headerData = strHeaders.get(0);
        assertTrue(headerData.split(",").length == 2);
        String[] linksData = headerData.split(",");
        assertTrue(linksData.length == 2);
        assertTrue(linksData[0].split(">;").length == 2);
        assertTrue(linksData[1].split(">;").length == 2);
        String expectedData = "</api/_search/example?page=0&size=0&template=Test4%3E&templateParams=Test5%3E%2CTest6%3E>; rel=\"last\","
            + "</api/_search/example?page=0&size=0&template=Test4%3E&templateParams=Test5%3E%2CTest6%3E>; rel=\"first\"";
        assertEquals(expectedData, headerData);
        List<String> xTotalCountHeaders = headers.get("X-Total-Count");
        assertTrue(xTotalCountHeaders.size() == 1);
        assertTrue(Long.valueOf(xTotalCountHeaders.get(0)).equals(0L));
    }
}
