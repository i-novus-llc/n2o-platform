package net.n2oapp.platform.selection.integration.repository;

import net.n2oapp.platform.selection.integration.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface ContactRepository extends JpaRepository<Contact, Integer> {

    @Query("SELECT c FROM Contact c WHERE c.owner.id IN (?1)")
    List<Contact> joinContacts(Collection<Integer> owners);

}
