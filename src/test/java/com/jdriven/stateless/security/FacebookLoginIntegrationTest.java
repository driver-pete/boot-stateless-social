package com.jdriven.stateless.security;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.net.URI;

import org.apache.commons.httpclient.Cookie;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { StatelessAuthentication.class })
@WebAppConfiguration
@IntegrationTest({})
public class FacebookLoginIntegrationTest {

    //private URL base;
    private RestTemplate template;
    private RestTemplate httpsTemplate;
    private String basePath;

    @Before
    public void setUp() throws Exception {
        this.template = new TestRestTemplate();
        this.basePath = "http://localhost:8080/";
        
        SSLContextBuilder builder = new SSLContextBuilder();
        // trust self signed certificate
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
                builder.build(),
                SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        final HttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(sslConnectionSocketFactory).build();

        this.httpsTemplate = new TestRestTemplate();
        this.httpsTemplate
                .setRequestFactory(new HttpComponentsClientHttpRequestFactory(
                        httpClient) {
                    @Override
                    protected HttpContext createHttpContext(
                            HttpMethod httpMethod, URI uri) {
                        HttpClientContext context = HttpClientContext.create();
                        RequestConfig.Builder builder = RequestConfig.custom()
                                .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                                .setAuthenticationEnabled(false)
                                .setRedirectsEnabled(false)
                                .setConnectTimeout(1000)
                                .setConnectionRequestTimeout(1000)
                                .setSocketTimeout(1000);
                        context.setRequestConfig(builder.build());
                        return context;
                    }
                });
    }

    @Test
    public void getAnonymousUser() throws Exception {
        ResponseEntity<User> response = template.getForEntity(
                this.basePath + "api/user/current", User.class);
        User user = response.getBody();
        assertNull(user.getUsername());
    }
    
    @Test
    public void getSecuredAnonymously() throws Exception {
        ResponseEntity<String> response = template.getForEntity(
                this.basePath + "api/restricted/generic", String.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.FORBIDDEN));
    }
    
    @Test
    public void loginFlow() throws Exception {
        ResponseEntity<String> response = template.getForEntity(
                this.basePath + "auth/facebook", String.class);
        assertTrue(response.getStatusCode().is3xxRedirection());
        URI loginRedirect = response.getHeaders().getLocation();
        assertThat(loginRedirect.toString(), startsWith("https://www.facebook.com/v1.0/dialog/oauth"));
        
        // Perform facebook login automation with HTMLUnit
        WebClient webClient = new WebClient();
        HtmlPage page1 = webClient.getPage(loginRedirect.toString());
        HtmlForm form = (HtmlForm) page1.getElementById("login_form");
        HtmlSubmitInput button = (HtmlSubmitInput) form.getInputsByValue("Log In").get(0);
        HtmlTextInput textField = form.getInputByName("email");
        textField.setValueAttribute("otognan@gmail.com");
        HtmlPasswordInput textField2 = form.getInputByName("pass");
        textField2.setValueAttribute("");
        HtmlPage homePage = button.click();
        // Check that we are redirected back to the application
        assertThat(homePage.getWebResponse().getRequestUrl().toString(), startsWith(this.basePath));
        Cookie tokenCookie = webClient.getCookieManager().getCookie("AUTH-TOKEN");
        assertNotNull(tokenCookie);
        String token = tokenCookie.getValue();
        assertNotNull(token);
        
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("X-AUTH-TOKEN", token);
        HttpEntity<String> requestEntity = new HttpEntity<String>(null, requestHeaders);
        ResponseEntity<User> loggedInUserResponse = template.exchange(this.basePath + "api/user/current",
                HttpMethod.GET, requestEntity, User.class);
        User user = loggedInUserResponse.getBody();
        assertThat(user.getUsername(), equalTo("Oleg"));
    }

}
