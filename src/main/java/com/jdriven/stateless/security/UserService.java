package com.jdriven.stateless.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService implements SocialUserService {

	@Autowired
	private UserRepository userRepo;

	@Override
	@Transactional(readOnly = true)
	public User loadUserByUserId(String userId)  {
		final User user = userRepo.findById(Long.valueOf(userId));
		return checkUser(user);
	}

	@Override
	@Transactional(readOnly = true)
	public User loadUserByUsername(String username) {
		final User user = userRepo.findByUsername(username);
		return checkUser(user);
	}

	@Override
	@Transactional(readOnly = true)
	public User loadUserByProviderIdAndProviderUserId(String providerId, String providerUserId) {
		final User user = userRepo.findByProviderIdAndProviderUserId(providerId, providerUserId);
		return checkUser(user);
	}

	@Override
	public void updateUserDetails(User user) {
		userRepo.save(user);
	}

	private User checkUser(User user) {
		if (user == null) {
			throw new UsernameNotFoundException("user not found");
		}
		/*
	     * This object throws standard exceptions if user account is locked, expired etc.
	     * Exception messages might be obtained from a message source.
	     */
		AccountStatusUserDetailsChecker checker = new AccountStatusUserDetailsChecker();
		checker.check(user);
		return user;
	}
}
