boot-stateless-social
===================
Example project integrating https://github.com/Robbert1/boot-stateless-auth with OAuth 2 based social login with facebook.

Add social.properties file to the src/main/resources with the following fields:
facebook.appKey=<your app id>
facebook.appSecret=<you app secret>

The build files and application.properties include commented out configuration for postgresql, mostly for testing behavior across server reboots.

Needs Gradle 2 and JDK 7

build with `gradle build`  
run with `gradle run`

===================

Architecture:
User - the main object which is stored in the database that represents the user with its roles,
    attributes, expiration, social id etc.
UserRepository - class that implements interactions with database that stores User objects.
SocialUserService - this class uses UserRepository to find and update users by some fields.
    It implements UserDetailsService for spring security and SocialUserDetailsService for spring social.
SimpleUsersConnectionRepository - ...

  