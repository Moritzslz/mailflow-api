package de.flowsuite.mailflowapi.settings;

import de.flowsuite.mailflowapi.common.entity.Settings;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface SettingsRepository extends CrudRepository<Settings, Long> {}
