package com.empOnboarding.api.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import com.empOnboarding.api.entity.Employee;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.empOnboarding.api.entity.Users;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserPrincipal implements UserDetails {

	private static final long serialVersionUID = 1L;

	private Long id;

	private String username;

	private String email;
	
	private String roleName;

	private Collection<? extends GrantedAuthority> authorities;

	public Long getId() {
		return id;
	}


	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public String getPassword() {
		return "";
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
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

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		UserPrincipal that = (UserPrincipal) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	public static UserDetails create(Users user) {
		List<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
		return new UserPrincipal(user.getId(),user.getName(), user.getEmail(),user.getRole(), authorities);
	}

	public static UserDetails createEmp(Employee emp) {
		List<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority("ROLE_" + emp.getRole()));
		return new UserPrincipal(emp.getId(),emp.getName(), emp.getEmail(),emp.getRole(), authorities);
	}
}
