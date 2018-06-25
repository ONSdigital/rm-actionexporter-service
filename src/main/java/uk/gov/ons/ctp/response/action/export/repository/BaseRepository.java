package uk.gov.ons.ctp.response.action.export.repository;

import java.io.Serializable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Extend Spring JPA CrudRepository to provide custom behaviour, optimisations.
 *
 * @param <T> the type of the entity to handle.
 * @param <ID> the type of the entity's identifier.
 */
@NoRepositoryBean
public interface BaseRepository<T, I extends Serializable> extends JpaRepository<T, I> {

  /**
   * Give visibility of underlying EntityManager persist method.
   *
   * @param entity to persist.
   * @param <S> persisted entity
   * @return <S> persisted entity.
   */
  <S extends T> S persist(S entity);
}
