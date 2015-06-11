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
TokenHandler - conversion from User to binary token and back
UserAuthenticationUserIdSource - returns user id that is currently in the security context


SocialAuthenticationSuccessHandler - TODO:
StatelessAuthenticationFilter - TODO:
TokenAuthenticationService - TODO:

Controllers:
UserController - important controller that returns currently logged in user by which js client determines
    what to do
FacebookController - fetches public facebook profile

Configs:
StatelessSocialConfig - ..
StatelessAuthenticationSecurityConfig - ..
StatelessAuthentication - main


 