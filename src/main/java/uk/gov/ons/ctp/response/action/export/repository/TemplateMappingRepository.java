package uk.gov.ons.ctp.response.action.export.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.ons.ctp.response.action.export.domain.TemplateMapping;

/** JPA repository for TemplateMapping entities */
@Repository
public interface TemplateMappingRepository extends JpaRepository<TemplateMapping, String> {}
