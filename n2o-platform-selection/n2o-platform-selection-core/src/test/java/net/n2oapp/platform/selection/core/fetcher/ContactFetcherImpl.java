package net.n2oapp.platform.selection.core.fetcher;

import net.n2oapp.platform.selection.core.domain.Contact;
import net.n2oapp.platform.selection.core.domain.ContactFetcher;

public class ContactFetcherImpl extends BaseModelFetcherImpl<Contact> implements ContactFetcher {

    public ContactFetcherImpl(Contact contact) {
        super(contact);
    }

    @Override
    public void fetchPhone(Contact model) {
        model.setPhone(src.getPhone());
    }

    @Override
    public void fetchEmail(Contact model) {
        model.setEmail(src.getEmail());
    }

    @Override
    public Contact create() {
        return new Contact();
    }

    @Override
    public void fetchId(Contact model) {
        model.setId(src.getId());
    }

}
