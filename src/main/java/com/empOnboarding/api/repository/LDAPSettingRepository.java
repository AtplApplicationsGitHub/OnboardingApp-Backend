package com.empOnboarding.api.repository;

import com.empOnboarding.api.entity.LdapSettingsInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LDAPSettingRepository extends JpaRepository<LdapSettingsInfo, Long> {

}