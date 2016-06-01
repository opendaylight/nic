package org.opendaylight.nic.of.renderer.exception;

import java.util.NoSuchElementException;

/**
 * Created by yrineu on 02/06/16.
 */
public class InvalidIntentParameterException extends NoSuchElementException {

    private static final String ERROR_MESSAGE = "Error when try to use an invalid parameter: ";

    public InvalidIntentParameterException(String message) {
        super(ERROR_MESSAGE + message);
    }
}
