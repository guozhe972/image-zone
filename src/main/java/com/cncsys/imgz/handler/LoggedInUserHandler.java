package com.cncsys.imgz.handler;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.cncsys.imgz.entity.AccountEntity.Authority;

public class LoggedInUserHandler extends HandlerInterceptorAdapter {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws IOException {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
		for (GrantedAuthority grantedAuthority : authorities) {
			switch (Authority.valueOf(grantedAuthority.getAuthority())) {
			case ADMIN:
				response.sendRedirect(request.getContextPath() + "/admin/home");
				return false;
			case USER:
				response.sendRedirect(request.getContextPath() + "/user/home");
				return false;
			case GUEST:
				response.sendRedirect(request.getContextPath() + "/guest/home");
				return false;
			case NONE:
				break;
			}
			break;
		}

		return true;
	}
}
