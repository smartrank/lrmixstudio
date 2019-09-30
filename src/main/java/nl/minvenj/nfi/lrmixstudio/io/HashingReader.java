/**
 * Copyright (C) 2013, 2014 Netherlands Forensic Institute
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package nl.minvenj.nfi.lrmixstudio.io;

import java.io.IOException;
import java.io.Reader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HashingReader extends Reader {

    private static final Logger LOG = LoggerFactory.getLogger(HashingReader.class);
    private static final String[] HEXDIGITS = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
    private final Reader parent;
    private MessageDigest hasher;
    private String hash = null;

    public HashingReader(Reader parent) {
        this.parent = parent;
        try {
            hasher = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException ex) {
            hasher = new XORDigest();
        }
    }

    public String getHash() {
        if (hash == null) {
            StringBuilder builder = new StringBuilder(hasher.getAlgorithm()).append("/");
            byte[] digestBytes = hasher.digest();
            for (int idx = 0; idx < digestBytes.length; idx++) {
                builder.append(HEXDIGITS[(digestBytes[idx] >> 4) & 0x0F]).append(HEXDIGITS[digestBytes[idx] & 0x0F]);
            }
            hash = builder.toString();
        }

        return hash;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        LOG.debug("Read {} {}", off, len);
        int value = parent.read(cbuf, off, len);
        if (value >= 0) {
            byte[] bytes = new byte[value];
            for (int idx = off; idx < (off + value); idx++) {
                bytes[idx - off] = (byte) cbuf[idx];
            }
            hasher.update(bytes, 0, value);
            hash = null;
        }
        return value;
    }

    @Override
    public void close() throws IOException {
        parent.close();
    }

    private static class XORDigest extends MessageDigest {

        byte digest;

        public XORDigest() {
            super("XOR");
            digest = 0;
        }

        @Override
        protected void engineUpdate(byte input) {
            digest ^= input;
        }

        @Override
        protected void engineUpdate(byte[] input, int offset, int len) {
            for (int idx = offset; idx < (offset + len); idx++) {
                update(input[idx]);
            }
        }

        @Override
        protected byte[] engineDigest() {
            return new byte[]{digest};
        }

        @Override
        protected void engineReset() {
            digest = (byte) 0xA5;
        }
    }
}
