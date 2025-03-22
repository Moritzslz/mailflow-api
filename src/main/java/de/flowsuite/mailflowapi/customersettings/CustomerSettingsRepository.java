package de.flowsuite.mailflowapi.customersettings;

import de.flowsuite.mailflowapi.common.entity.CustomerSettings;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface CustomerSettingsRepository extends CrudRepository<CustomerSettings, Long> {}
