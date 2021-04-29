package net.n2oapp.platform.selection.integration.fetcher;

import net.n2oapp.platform.selection.integration.model.Contact;
import net.n2oapp.platform.selection.integration.model.ContactFetcher;
import net.n2oapp.platform.selection.integration.model.ContactSelection;
import org.springframework.lang.NonNull;

public class ContactFetcherImpl extends BaseModelFetcherImpl<Contact, ContactSelection> implements ContactFetcher<Contact> {

    public ContactFetcherImpl(Contact contact) {
        super(contact);
    }

    @Override
    public @NonNull Contact create() {
        return new Contact();
    }

    @Override
    public String fetchPhone() {
        return src.getPhone();
    }

    @Override
    public String fetchEmail() {
        return src.getEmail();
    }

}
