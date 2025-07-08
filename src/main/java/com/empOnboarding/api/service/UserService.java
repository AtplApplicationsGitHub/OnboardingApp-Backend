package com.empOnboarding.api.service;

import java.io.ByteArrayInputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.empOnboarding.api.entity.SpringSession;
import com.empOnboarding.api.entity.SpringSessionAttributes;
import com.empOnboarding.api.entity.Users;
import com.empOnboarding.api.repository.SessionAttrRepository;
import com.empOnboarding.api.repository.SessionDAO;

@Service
public class UserService {
	private final SessionAttrRepository sessionAttrDAO;
	private final SessionDAO sessionDAO;

	public UserService(SessionDAO sessionDAO, SessionAttrRepository sessionAttrDAO) {
		this.sessionAttrDAO = sessionAttrDAO;
		this.sessionDAO = sessionDAO;

	}

	@Transactional
	public boolean isActiveSessionPresent(Long userId) throws Exception {
		boolean result = false;
		try {
			List<SpringSessionAttributes> attributes = sessionAttrDAO.findByIdAttributeName("id");
			if (userId != null && userId != 0l) {
				for (SpringSessionAttributes attr : attributes) {
					ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(attr.getAttributeBytes()));
					String id = (String) in.readObject();
					if (id != null && id.equalsIgnoreCase(userId.toString())) {
						Date expireDateTime = new Date(attr.getSpringSession().getExpiryTime());
						Date currentdateTime = new Date();
						if (expireDateTime.after(currentdateTime)) {
							return true;
						} else {
							sessionDAO.delete(new SpringSession(attr.getSpringSession().getPrimaryId()));
						}
					}
				}
			}
		} catch (Exception e) {
			throw e;
		}
		return result;
	}

	@Transactional
	public boolean isActiveSessionPresent(Users userDetails, String jwt) throws Exception {
		boolean result = false;
		try {
			List<SpringSessionAttributes> authAttrs = sessionAttrDAO.findByIdAttributeName("authToken");
			if (userDetails != null && userDetails.getName() != null) {
				for (SpringSessionAttributes attr : authAttrs) {
					ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(attr.getAttributeBytes()));
					String dbToken = (String) in.readObject();
					if (dbToken != null && (dbToken.equalsIgnoreCase(jwt))) {
						Date expireDateTime = new Date(attr.getSpringSession().getExpiryTime());
						Date currentdateTime = new Date();
						if (expireDateTime.after(currentdateTime)) {
							return true;
						} else {
							sessionDAO.delete(new SpringSession(attr.getSpringSession().getPrimaryId()));
						}
						break;
					}
				}
			}
		} catch (Exception e) {
			throw e;
		}
		return result;
	}

	@Transactional
	public String getPrimaryId(String sessionId) throws Exception {
		String primaryKey = null;
		try {
			Optional<SpringSession> sessionObject = sessionDAO.findBySessionId(sessionId);
			if (sessionObject.isPresent()) {
				SpringSession session = sessionObject.get();
				primaryKey = session.getPrimaryId();
			}
		} catch (Exception e) {
			throw e;
		}
		return primaryKey;
	}

	@Transactional
	public String getSessionId(Users userDetails) throws Exception {
		String sessionId = null;
		try {
			List<SpringSessionAttributes> attributes = sessionAttrDAO.findByIdAttributeName("id");
			if (userDetails != null && userDetails.getName() != null) {
				for (SpringSessionAttributes attr : attributes) {
					ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(attr.getAttributeBytes()));
					String username = (String) in.readObject();
					if (username != null && username.equalsIgnoreCase(userDetails.getId().toString())) {
						Date expireDateTime = new Date(attr.getSpringSession().getExpiryTime());
						Date currentdateTime = new Date();
						if (expireDateTime.after(currentdateTime))
							sessionId = attr.getSpringSession().getSessionId();
					}
				}
			}
		} catch (Exception e) {
			throw e;
		}
		return sessionId;
	}

	@Transactional
	public long getsessionCreatedTime(String sessionId) throws Exception {
		long createdtime = System.currentTimeMillis();
		try {
			Optional<SpringSession> sessionObject = sessionDAO.findBySessionId(sessionId);
			if (sessionObject.isPresent()) {
				SpringSession session = sessionObject.get();
				Date expireDateTime = new Date(session.getExpiryTime());
				Date currentdateTime = new Date();
				if (expireDateTime.after(currentdateTime))
					createdtime = session.getCreationTime();
			}
		} catch (Exception e) {
			throw e;
		}
		return createdtime;
	}
}
