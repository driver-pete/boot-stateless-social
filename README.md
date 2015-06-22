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
Spring social core objects:
Connection - user-specific object that stores necessary data (susch as token) to access a particular social
    provider (e.g Facebook) for a particular user. Connection can be persisted so that application can
    access the user profile without additional login screen.
ConnectionFactory - object that can create Connection for a user. Typically it has to be associated with
    provider application (such as Facebook application). User grants rights to the application to act on its
    behalf and have access to his data. After that the application (and therefore ConnectionFactory) can
    create a Connection.
ConnectionFactoryRegistry - this object provides access to Connection factories
    based on provider id (such as "Facebook"). It is populated via ConnectionFactoryConfigurer, which in turn
    is configured in StatelessSocialConfig.
ConnectionRepository - interface for saving and restoring Connection objects from a persistent store. An object
    is specific to a particular user (one ConnectionRepository for one user) but can span accross multiple
    social providers.
UsersConnectionRepository - interface for saving and restoring Connection objects from a persistent store.
    Operates accross all users and not specific to the single user.
ConnectionSignUp - interface creates user id from new Connection. Usually it presents signup form for
    creating an account with the application.

===================
Application objects:
User - the main object which is stored in the database that represents the user with its roles,
    attributes, expiration, social id etc.
UserRepository - class that implements interactions with database that stores User objects.
SocialUserService - this class uses UserRepository to find and update users by some fields.
    It implements UserDetailsService for spring security and SocialUserDetailsService for spring social.
AutoSignUpHandler - automatically (without sign-up form) creates a new User from the Connection and
    saves it to UserRepository.
TemporaryConnectionRepository - in-memory ConnectionRepository
SimpleUsersConnectionRepository - UsersConnectionRepository that uses SocialUserService,
    ConnectionFactoryLocator, TemporaryConnectionRepository, and AutoSignUpHandler to retreive
    and update Connections for users.
UserAuthenticationUserIdSource - returns user id that is currently in the security context

TokenHandler - conversion from User to binary token and back
TokenAuthenticationService - uses TokenHandler to add user to response cookie.
    Also reads User from the request header. 
StatelessAuthenticationFilter - uses TokenAuthenticationService to get User from header of every request
    and puts user into the SecurityContext
SocialAuthenticationSuccessHandler - implementation of SavedRequestAwareAuthenticationSuccessHandler which
   is called after successful authentication with the original web request and response. Usually it redirects
   to the page that has been called by the nonauth. user before the authentication so that user sees the page
   he requested. SocialAuthenticationSuccessHandler loads user based on username and adds cookie to the response
   using TokenAuthenticationService.


Controllers:
UserController - important controller that returns currently logged in user by which js client determines
    what to do
FacebookController - fetches public facebook profile

Configs:
StatelessSocialConfig - set up Social classes
StatelessAuthenticationSecurityConfig - set up security classes
StatelessAuthentication - main

===================

Flow of events:
User hits the root '/' which is not protected,
User receives index.html with javascript client.
Client does not have token.
User hits button 'login with facebook'.
Client hits /auth/facebook.
Filter SocialAuthenticationFilter "requiresAuthentication" returns true because facebook is registered as provider.
Facebook OAuth2AuthenticationService processes the request:
 - it tries to get token from it and fails with AuthenticationServiceException
 - Facebook connection factory creates connection and build redirect url
 - exception is raised which is handles by SocialAuthenticationFailureHandler
 - it generates redirect to facebook
User authenticate himself and the app on facebbok
After finishing auth on facebook, facebook calls /auth/facebook with the code
Facebook OAuth2AuthenticationService processes the request:
 - it successfully extracts token from the code
 - there is Connection in the token already
 - AuthenticationManager.authenticate(token)
   - SocialAuthenticationProvider uses Connection from the token
      - to call findUserIdsWithConnection of SimpleUsersConnectionRepository
      - SimpleUsersConnectionRepository tries to find user in the userRepo and fails
      - because of exception, connectionSignUp.execute(connection) is called
      - AutoSignUpHandler adds user to the repository
   - SocialAuthenticationProvider is unaware of the all that gets id of the newly added user
 - AuthenticationManager authorization is successful
 - updateConnections(authService, token, success);
   - TemporaryConnectionRepository is created for this user
   - Connection is added to it
 - SocialAuthenticationSuccessHandler on success is called
   - token is added into 'AUTH-TOKEN' cookie
 
Client gets cookie 'AUTH-TOKEN', client reads token from it and deletes the cookie.
Client stores the token.
Client is in the authenticated state.
Client uses the token to access /api/user/current
...
Client receives populated User object.
Client switches to authenticated state because it knows that token is valid.
What is client receives bad User object?


Security of client localStorage: 
https://blog.whitehatsec.com/web-storage-security/

If client requests User json object using GET /api/user/current without token
Anonymous authentication is created.
UserController returns default empty User object.
Empty User object is propagated to client as json.

 