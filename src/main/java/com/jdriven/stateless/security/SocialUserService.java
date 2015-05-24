package com.jdriven.stateless.security;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.social.security.SocialUserDetailsService;

public interface SocialUserService extends SocialUserDetailsService, UserDetailsService {
    
    /*
     * from UserDetailsService: load UserDetails by username.
     * User implements SocialUserDetails which in turn implements UserDetails
     */
    User loadUserByUsername(String username);
    
    /*
     * from SocialUserDetailsService: load SocialUserDetails by user id.
     * User implements SocialUserDetails
     */
    User loadUserByUserId(String userId);
    
    /*
     * This function used in UsersConnectionRepository in order to find user from its Facebook connection
     * after user logs in in Facebook
     */
    User loadUserByProviderIdAndProviderUserId(String providerId, String providerUserId);

    /*
     * This function is used in UsersConnectionRepository to update user's access token from
     * Facebook connection information.
     */
    void updateUserDetails(User user);
}
