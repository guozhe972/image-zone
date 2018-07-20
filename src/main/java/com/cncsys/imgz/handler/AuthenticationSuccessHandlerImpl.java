package com.cncsys.imgz.handler;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.cncsys.imgz.entity.AccountEntity.Authority;

public class AuthenticationSuccessHandlerImpl implements AuthenticationSuccessHandler {

	@Override
	public void onAuthenticationSuccess(
			HttpServletRequest request, HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {

		String url = "/";
		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
		for (GrantedAuthority grantedAuthority : authorities) {
			switch (Authority.valueOf(grantedAuthority.getAuthority())) {
			case ADMIN:
				url = "/admin/home";
				break;
			case USER:
				url = "/user/home";
				break;
			case GUEST:
				url = "/guest/home";
				break;
			case NONE:
				throw new ServletException("access denied");
			}
			break;
		}

		RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
		redirectStrategy.sendRedirect(request, response, url);
	}
}
