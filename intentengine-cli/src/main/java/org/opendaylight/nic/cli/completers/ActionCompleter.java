package org.opendaylight.nic.cli.completers;

import java.util.List;

import org.apache.karaf.shell.console.completer.StringsCompleter;
import org.apache.karaf.shell.console.Completer;

/**
 * <p>
 * A very simple completer.
 * </p>
 */
public class ActionCompleter implements Completer {

 /**
  * @param buffer the beginning string typed by the user
  * @param cursor the position of the cursor
  * @param candidates the list of completions proposed to the user
  */
@Override
public int complete(String buffer, int cursor, List<String> candidates) {
	StringsCompleter delegate = new StringsCompleter();
    delegate.getStrings().add("ALLOW");
    delegate.getStrings().add("BLOCK");
    return delegate.complete(buffer, cursor, candidates);
}

}