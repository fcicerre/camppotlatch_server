package org.coursera.camppotlatch.service.auth;

import java.util.Collection;

import org.coursera.camppotlatch.service.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

public class UserAuthenticationDetails implements UserDetails {
	private static final long serialVersionUID = 1L;
	
	private final String username_;
	
	private String password_;
	private Collection<? extends GrantedAuthority> authorities_;

	public UserAuthenticationDetails(User user) {
		this(user.getLogin(), user.getPassword(), user.getRoles());
	}
	
	public UserAuthenticationDetails(String username, String password, String roles) {
		this(username, password, AuthorityUtils.commaSeparatedStringToAuthorityList(roles));
	}
	
	public UserAuthenticationDetails(String username, String password,
			Collection<? extends GrantedAuthority> authorities) {
		username_ = username;
		password_ = password;
		authorities_ = authorities;
	}

	@Override
	public String getUsername() {
		return username_;
	}

	@Override
	public String getPassword() {
		return password_;
	}

	public void setPassword(String password) {
		password_ = password;
	}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities_;
	}

	public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
		authorities_ = authorities;
	}
	
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
