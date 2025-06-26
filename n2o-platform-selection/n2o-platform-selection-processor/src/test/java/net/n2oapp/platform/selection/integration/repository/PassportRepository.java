package net.n2oapp.platform.selection.integration.repository;

import net.n2oapp.platform.selection.integration.model.Passport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PassportRepository extends JpaRepository<Passport, Integer> {

    @Query("SELECT p FROM Passport p WHERE p IN (SELECT e.passport FROM Employee e WHERE e.id IN (?1))")
    List<Passport> joinPassport(List<Integer> employees);

}
