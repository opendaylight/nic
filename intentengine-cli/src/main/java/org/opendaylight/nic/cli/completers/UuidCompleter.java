package org.opendaylight.nic.cli.completers;

import java.util.List;

import org.apache.karaf.shell.console.Completer;
import org.apache.karaf.shell.console.completer.StringsCompleter;
import org.opendaylight.nic.api.NicConsoleProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;

public class UuidCompleter implements Completer {

    protected NicConsoleProvider provider;

    public UuidCompleter(NicConsoleProvider provider) {
        this.provider = provider;
    }

    /**
     * @param buffer
     *            the beginning string typed by the user
     * @param cursor
     *            the position of the cursor
     * @param candidates
     *            the list of completions proposed to the user
     */
    @Override
    public int complete(String buffer, int cursor, List<String> candidates) {

        String query = (buffer == null) ? ".*" : buffer;

        if (!query.contains("*"))
            query.concat("*");

        StringsCompleter delegate = new StringsCompleter();

        for (Intent intent : provider.listIntents(true)) {
            String id = intent.getId().getValue();
            if (id.matches(query)) {
                candidates.add(id);
                delegate.getStrings().add(id);
            }
        }

        return delegate.complete(buffer, cursor, candidates);
    }
}