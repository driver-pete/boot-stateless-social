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

    @Before
    public void setUp() throws Exception {
        //this.base = new URL("https://localhost:" + port + "/hello");
        this.template = new TestRestTemplate();
    }

    @Test
    public void getAnonymousUser() throws Exception {
        ResponseEntity<User> response = template.getForEntity(
                "http://localhost:8080/api/user/current", User.class);
        User user = response.getBody();
        assertNull(user.getUsername());
    }

}
