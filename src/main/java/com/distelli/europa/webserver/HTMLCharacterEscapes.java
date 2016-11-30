/*
 * @author Boris Veltman [boris]
 * (c) Copyright 2016 Distelli Inc., all rights reserved
 */
package com.distelli.europa.webserver;

import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.SerializedString;

//See discussion for reference: http://stackoverflow.com/questions/6817520/escape-forward-slash-in-jackson
public class HTMLCharacterEscapes extends CharacterEscapes
{
    private final int[] asciiEscapes;

    public HTMLCharacterEscapes()
    {
        // start with set of characters known to require escaping (double-quote, backslash etc)
        int[] esc = CharacterEscapes.standardAsciiEscapesForJSON();
        // and force escaping slash to avoid the </script> problem:
        esc['/'] = CharacterEscapes.ESCAPE_CUSTOM;
        asciiEscapes = esc;
    }
    // this method gets called for character codes 0 - 127
    @Override public int[] getEscapeCodesForAscii() {
        return asciiEscapes;
    }
    // and this for others; we don't need anything special here
    @Override public SerializableString getEscapeSequence(int ch) {
        // no further escaping (beyond ASCII chars) needed:
        return new SerializedString("\\/");
    }
}
