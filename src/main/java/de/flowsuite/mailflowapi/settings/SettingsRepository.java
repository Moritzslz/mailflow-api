package de.flowsuite.mailflowapi.settings;

import de.flowsuite.mailflowcommon.entity.Settings;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface SettingsRepository extends CrudRepository<Settings, Long> {

    boolean existsByUserId(long userId);
}
