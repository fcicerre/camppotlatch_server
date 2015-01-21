package org.coursera.camppotlatch.service.auth;

import java.util.Collection;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

import org.coursera.camppotlatch.service.commons.AppContext;
import org.coursera.camppotlatch.service.model.User;
import org.coursera.camppotlatch.service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;

public class PersistentUserDetailsManager implements UserDetailsManager, UserDetailsService {
	//@Autowired
	private UserRepository userRepository;
	
	private SortedMap<String, UserAuthenticationDetails> userDetailsMap = 
			new TreeMap<String, UserAuthenticationDetails>();
	
	public PersistentUserDetailsManager() {
		this(Collections.<UserAuthenticationDetails> emptyList());
	}
	
	public PersistentUserDetailsManager(Collection<UserAuthenticationDetails> usersDetails) {
		super();
		for (UserAuthenticationDetails userDetails : usersDetails) {
			userDetailsMap.put(userDetails.getUsername(), userDetails);
		}
		
		userRepository = AppContext.userRepository();
	}
	
	@Override
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException {
		UserAuthenticationDetails userDetails = userDetailsMap.get(username);
		
		// Try to find in the database if it is not in the cache
		User user = null;
		if (userDetails == null) {
			user = userRepository.findByLogin(username);
			if (user == null)
				throw new UsernameNotFoundException("There is no " + username + " user");
			
			userDetails = new UserAuthenticationDetails(user);
		}		
		
		return userDetails;
	}

	@Override
	public void createUser(UserDetails userDetails) {
		String login = userDetails.getUsername();
		String password = userDetails.getPassword();
		
		UserAuthenticationDetails retrievedUserDetails = userDetailsMap.get(login);
		if (retrievedUserDetails != null)
			return;
		
		User retrievedUser = userRepository.findByLogin(login);
		if (retrievedUser != null)
			return;
		
		String roles = "";
		Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
		for (GrantedAuthority authority : authorities) {
			if (!roles.equals(""))
				roles += ",";
			roles += authority.getAuthority();
		}
		
		// Create this user into the database
		User user = new User(login, password, roles);
		userRepository.save(user);
	}

	@Override
	public void updateUser(UserDetails userDetails) {
		String login = userDetails.getUsername();
		String password = userDetails.getPassword();
		
		UserAuthenticationDetails retrievedUserDetails = userDetailsMap.get(login);
		if (retrievedUserDetails != null) {
			retrievedUserDetails.setAuthorities(userDetails.getAuthorities());
			return;
		}
		
		// Try to find in the database if it is not in the cache 
		User retrievedUser = userRepository.findByLogin(login);
		if (retrievedUser != null) {
			String roles = "";
			Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
			for (GrantedAuthority authority : authorities) {
				if (!roles.equals(""))
					roles += ",";
				roles += authority.getAuthority();
			}
			
			// Save this user into the database
			retrievedUser.setRoles(roles);
			userRepository.save(retrievedUser);
		}
	}

	@Override
	public void deleteUser(String username) {
		userDetailsMap.remove(username);
		
		// Remove the user from the database
		userRepository.delete(username);
	}

	@Override
	public void changePassword(String oldPassword, String newPassword) {
		// No effects -- it is not possible to find a user from his password
	}

	@Override
	public boolean userExists(String username) {
		UserAuthenticationDetails userDetails = userDetailsMap.get(username);
		if (userDetails != null)
			return true;
		
		// Try to find in the database if it is not in the cache
		User user = userRepository.findByLogin(username);
		if (user != null)
			return true;
		
		return false;
	}

}
