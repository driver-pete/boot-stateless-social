package com.jdriven.stateless.security;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;

import org.junit.Assert;

import java.net.URI;
import java.net.URL;



//import org.apache.commons.lang3.StringUtils;
////import org.apache.http.HttpHeaders;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.config.CookieSpecs;
//import org.apache.http.client.config.RequestConfig;
//import org.apache.http.client.protocol.HttpClientContext;
//import org.apache.http.client.utils.URIBuilder;
//import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
//import org.apache.http.conn.ssl.SSLContextBuilder;
//import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
//import org.apache.http.impl.client.HttpClients;
//import org.apache.http.protocol.HttpContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;

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
//@Order(1)
//// default order of WebSecurityConfig is 100, so this config has a priority
//class TestWebSecurityConfig extends WebSecurityConfig {
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http.authorizeRequests().antMatchers("/facebook_mock/**").permitAll();
//        super.configure(http);
//    }
//}

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { StatelessAuthentication.class })
@WebAppConfiguration
@IntegrationTest({})
public class FacebookLoginIntegrationTest {

    //private URL base;
    private RestTemplate template;
    private String basePath;

    @Before
    public void setUp() throws Exception {
        this.template = new TestRestTemplate();
        this.basePath = "http://localhost:8080/";
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
