package com.empOnboarding.api.security;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import com.empOnboarding.api.entity.Employee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.empOnboarding.api.entity.SpringSession;
import com.empOnboarding.api.entity.SpringSessionAttributes;
import com.empOnboarding.api.entity.Users;
import com.empOnboarding.api.repository.SessionAttrRepository;
import com.empOnboarding.api.repository.SessionDAO;
import com.empOnboarding.api.service.UserService;
public class AppAuthenticationFilter extends OncePerRequestFilter {

	@Value("${spring.session.timeout.seconds}")
    private String maxActive;    
	
    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private AppCustomUserDetailsService customUserDetailsService;
    
    @Autowired
    UserService userService;
    
    @Autowired
    SessionDAO sessionDAO;
    
    @Autowired
    SessionAttrRepository sessionAttrDAO;

    private static final Logger logger = LoggerFactory.getLogger(AppAuthenticationFilter.class);

    @Override
    @Transactional
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);
			if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
				Long userId = tokenProvider.getUserIdFromJWT(jwt);
				String role = tokenProvider.getRoleFromJWT(jwt);
				if(role.equalsIgnoreCase("Admin")) {
					Users userData = customUserDetailsService.loadUserByUserId(userId);
					if(userService.isActiveSessionPresent(userData,jwt)) {
						SpringSession springSession = populateSpringSession(userData);
						sessionDAO.saveAndFlush(springSession);
						UserDetails userDetails = customUserDetailsService.loadUserById(userId);
						UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
						authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
						SecurityContextHolder.getContext().setAuthentication(authentication);
					}
				}else{
					Employee userData = customUserDetailsService.loadEmployeeByEmpId(userId);
					if(userService.isActiveSessionPresentEmp(userData,jwt)) {
						SpringSession springSession = populateSpringSessionEmp(userData);
						sessionDAO.saveAndFlush(springSession);
						UserDetails userDetails = customUserDetailsService.loadEmployeeById(userId);
						UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
						authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
						SecurityContextHolder.getContext().setAuthentication(authentication);
					}
				}
			}
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7, bearerToken.length());
        }
        return null;
    } 
    
    @Transactional
	private SpringSession populateSpringSession(Users userDetails) throws Exception {
		SpringSession springSession = new SpringSession();
		try {
			String sessionId = userService.getSessionId(userDetails);
			String primaryId = userService.getPrimaryId(sessionId);
			long createdTime = userService.getsessionCreatedTime(sessionId);
			springSession.setCreationTime(createdTime);
			springSession.setExpiryTime(new Date(System.currentTimeMillis()+20*60*1000).getTime());
			springSession.setLastAccessTime(new Date().getTime());
			springSession.setMaxInactiveInterval(Integer.valueOf(maxActive));
			springSession.setPrimaryId(primaryId);
			springSession.setSessionId(sessionId);
			List<SpringSessionAttributes> attributes = sessionAttrDAO.findBySpringSession(springSession);
			springSession.setSpringSessionAttributeses(attributes.stream().collect(Collectors.toSet()));
		} catch (Exception e) {
			throw e;
		}
		return springSession;
	}

	@Transactional
	private SpringSession populateSpringSessionEmp(Employee userDetails) throws Exception {
		SpringSession springSession = new SpringSession();
		try {
			String sessionId = userService.getSessionIdForEmp(userDetails);
			String primaryId = userService.getPrimaryId(sessionId);
			long createdTime = userService.getsessionCreatedTime(sessionId);
			springSession.setCreationTime(createdTime);
			springSession.setExpiryTime(new Date(System.currentTimeMillis()+20*60*1000).getTime());
			springSession.setLastAccessTime(new Date().getTime());
			springSession.setMaxInactiveInterval(Integer.valueOf(maxActive));
			springSession.setPrimaryId(primaryId);
			springSession.setSessionId(sessionId);
			List<SpringSessionAttributes> attributes = sessionAttrDAO.findBySpringSession(springSession);
			springSession.setSpringSessionAttributeses(attributes.stream().collect(Collectors.toSet()));
		} catch (Exception e) {
			throw e;
		}
		return springSession;
	}
}
