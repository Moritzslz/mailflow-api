package de.flowsuite.mailflow.api.settings;

import de.flowsuite.mailflow.common.entity.Settings;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface SettingsRepository extends CrudRepository<Settings, Long> {

    boolean existsByUserId(long userId);
}
