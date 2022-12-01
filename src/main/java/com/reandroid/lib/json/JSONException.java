package com.reandroid.lib.json;

public class JSONException extends IllegalArgumentException {
    /** Serialization ID */
    private static final long serialVersionUID = 0;

    public JSONException(final String message) {
        super(message);
    }

    public JSONException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public JSONException(final Throwable cause) {
        super(cause.getMessage(), cause);
    }

}
