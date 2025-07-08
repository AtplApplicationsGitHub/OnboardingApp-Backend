package com.empOnboarding.api.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class SpringSessionAttributesId implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	private String sessionPrimaryId;
	private String attributeName;

	public SpringSessionAttributesId() {
	}

	public SpringSessionAttributesId(String sessionPrimaryId, String attributeName) {
		this.sessionPrimaryId = sessionPrimaryId;
		this.attributeName = attributeName;
	}

	@Column(name = "SESSION_PRIMARY_ID", nullable = false, length = 36)
	public String getSessionPrimaryId() {
		return this.sessionPrimaryId;
	}

	public void setSessionPrimaryId(String sessionPrimaryId) {
		this.sessionPrimaryId = sessionPrimaryId;
	}

	@Column(name = "ATTRIBUTE_NAME", nullable = false, length = 200)
	public String getAttributeName() {
		return this.attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof SpringSessionAttributesId))
			return false;
		SpringSessionAttributesId castOther = (SpringSessionAttributesId) other;

		return ((this.getSessionPrimaryId() == castOther.getSessionPrimaryId())
				|| (this.getSessionPrimaryId() != null && castOther.getSessionPrimaryId() != null
						&& this.getSessionPrimaryId().equals(castOther.getSessionPrimaryId())))
				&& ((this.getAttributeName() == castOther.getAttributeName())
						|| (this.getAttributeName() != null && castOther.getAttributeName() != null
								&& this.getAttributeName().equals(castOther.getAttributeName())));
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result + (getSessionPrimaryId() == null ? 0 : this.getSessionPrimaryId().hashCode());
		result = 37 * result + (getAttributeName() == null ? 0 : this.getAttributeName().hashCode());
		return result;
	}

}

