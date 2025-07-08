package com.empOnboarding.api.entity;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "SPRING_SESSION_ATTRIBUTES")
public class SpringSessionAttributes implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	private SpringSessionAttributesId id;
	private SpringSession springSession;
	private byte[] attributeBytes;

	public SpringSessionAttributes() {
	}

	public SpringSessionAttributes(SpringSessionAttributesId id, SpringSession springSession, byte[] attributeBytes) {
		this.id = id;
		this.springSession = springSession;
		this.attributeBytes = attributeBytes;
	}

	@EmbeddedId
	@AttributeOverrides({
			@AttributeOverride(name = "sessionPrimaryId", column = @Column(name = "SESSION_PRIMARY_ID", nullable = false, length = 36)),
			@AttributeOverride(name = "attributeName", column = @Column(name = "ATTRIBUTE_NAME", nullable = false, length = 200)) })
	public SpringSessionAttributesId getId() {
		return this.id;
	}

	public void setId(SpringSessionAttributesId id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "SESSION_PRIMARY_ID", nullable = false, insertable = false, updatable = false)
	public SpringSession getSpringSession() {
		return this.springSession;
	}

	public void setSpringSession(SpringSession springSession) {
		this.springSession = springSession;
	}

	@Column(name = "ATTRIBUTE_BYTES", nullable = false)
	public byte[] getAttributeBytes() {
		return this.attributeBytes;
	}

	public void setAttributeBytes(byte[] attributeBytes) {
		this.attributeBytes = attributeBytes;
	}

}

