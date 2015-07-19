package com.jdriven.stateless.security;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertNotNull;
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
import org.springframework.http.HttpMethod;
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


/*
 * Extra security config that opens access to test controller.
 * Here we extend normal WebSecurityConfig and add allowed url before
 * the more strict rules defined by WebSecurityConfig because spring
 * evaluates antMatchers in the order they are declared.
 * 
 * It doesn't work to extend WebSecurityConfigurerAdapter instead of 
 * main config for some reason.
 * 
 */
//@Configuration
//@EnableSocial
//@Order(1)
////@Profile("test")
//// default order of WebSecurityConfig is 100, so this config has a priority
//class TestStatelessSocialConfig extends StatelessSocialConfig {
//    @Override
//    public void addConnectionFactories(ConnectionFactoryConfigurer cfConfig, Environment env) {
//        System.out.println("TEST_HELLO");
//        cfConfig.addConnectionFactory(new FacebookConnectionFactory(
//                env.getProperty("facebook.appKey"),
//                env.getProperty("facebook.appSecret")));
//    }
//}

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { StatelessAuthentication.class })
@WebAppConfiguration
@IntegrationTest({})
//@ActiveProfiles("test")
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

//    @Test
//    public void getAnonymousUser() throws Exception {
//        ResponseEntity<User> response = template.getForEntity(
//                this.basePath + "api/user/current", User.class);
//        User user = response.getBody();
//        assertNull(user.getUsername());
//    }
//    
//    @Test
//    public void getSecuredAnonymously() throws Exception {
//        ResponseEntity<String> response = template.getForEntity(
//                this.basePath + "api/restricted/generic", String.class);
//        assertThat(response.getStatusCode(), equalTo(HttpStatus.FORBIDDEN));
//    }
    
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
        textField2.setValueAttribute();
        HtmlPage homePage = button.click();
        // Check that we are redirected back to the application
        assertThat(homePage.getWebResponse().getRequestUrl().toString(), startsWith(this.basePath));
        Cookie tokenCookie = webClient.getCookieManager().getCookie("AUTH-TOKEN");
        assertNotNull(tokenCookie);
        String token = tokenCookie.getValue();
        
        System.out.println("--------------------------------------");
        System.out.println(token);
        
        
//        ResponseEntity<String> facebookResponse = this.httpsTemplate.getForEntity(
//                loginRedirect.toString(), String.class);
//        System.out.println(loginRedirect.toString());
//        System.out.println("-----------");
//        System.out.println(facebookResponse.getHeaders());
//        System.out.println("-----------");
//        System.out.println(facebookResponse.getBody());
//        assertTrue(true);
    }
    
//    @Test
//    public void testAuthentication() throws Exception {
//
//        String greetingsUrl = new URL("https://localhost:" + port
//                + "/test_greeting").toString();
//        ResponseEntity<String> initialResponse = template.getForEntity(
//                greetingsUrl, String.class);
//        System.out.println("INITIAL RESPONSE "
//                + initialResponse.getHeaders().getFirst("Set-Cookie"));
//        // Expect redirect to the server login page
//        assertTrue(initialResponse.getStatusCode().is3xxRedirection());
//        URI loginRedirect = initialResponse.getHeaders().getLocation();
//        System.out.println(loginRedirect.getPath());
//        assertThat(loginRedirect.getPath(), equalTo("/auth/facebook"));
//        ResponseEntity<String> springLoginResponse = template.getForEntity(
//                loginRedirect.toString(), String.class);
//
//        // Expect redirect to the facebook login page
//        assertTrue(springLoginResponse.getStatusCode().is3xxRedirection());
//        URI facebookLoginRedirect = springLoginResponse.getHeaders()
//                .getLocation();
//        assertThat(facebookLoginRedirect.getPath(),
//                equalTo("/facebook_mock/dialog/oauth"));
//
//        // Lets got to login page now
//        System.out.println(facebookLoginRedirect);
//        ResponseEntity<String> facebookLoginResponse = template.getForEntity(
//                facebookLoginRedirect.toString(), String.class);
//
//        // Here we emulate posting to the login form. We assume that facebook
//        // mock
//        // returned a login link like on a form. We just add login to it and
//        // post
//        String email = "testuser@gmail.com";
//        URI loginFormURI = URI.create("https://localhost:8443/facebook_mock"
//                + facebookLoginResponse.getBody() + "&email=" + email);
//        ResponseEntity<String> loginFormResponse = template.getForEntity(
//                loginFormURI.toString(), String.class);
//        assertTrue(loginFormResponse.getStatusCode().is2xxSuccessful());
//
//        // We except a good session cookie after succesful login
//        String jsonid = StringUtils.substringBetween(loginFormResponse
//                .getHeaders().getFirst("Set-Cookie"), "JSESSIONID=", ";");
//        assertTrue(jsonid != null);
//
//        // Now we supposed to be logged in. Redirect on the past request is
//        // broken, so we just
//        // have to ask for secured page again adding a session cookie:
//
//        HttpHeaders requestHeaders = new HttpHeaders();
//        requestHeaders.add("Cookie", "JSESSIONID=" + jsonid);
//        HttpEntity<String> requestEntity = new HttpEntity<String>(null,
//                requestHeaders);
//        ResponseEntity<String> finalResponse = template.exchange(greetingsUrl,
//                HttpMethod.GET, requestEntity, String.class);
//
//        assertThat(finalResponse.getBody(), equalTo("Greetings to " + email));
//    }

}
