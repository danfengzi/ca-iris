/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002  Minnesota Department of Transportation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package us.mn.state.dot.tms.comm.ntcip;

import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import us.mn.state.dot.tms.comm.ParsingException;

/**
 * Basic Encoding Rules for ASN.1
 *
 * @author Douglas Lau
 */
abstract public class BER extends ASN1 {

	/** Constant to check the high bit of a byte */
	static public final byte HIGH_BIT = (byte)0x80;

	/** Constant to check the low seven bits of a byte */
	static public final byte SEVEN_BITS = 0x7F;

	/** Reserved length code constant */
	static public final int RESERVED = 0xFF;

	/** Encode a BER identifier to the output stream */
	protected void encodeIdentifier(Tag tag) throws IOException {
		byte first = tag.clazz;
		if(tag.constructed) first |= Tag.CONSTRUCTED;
		if(tag.number < Tag.ONE_OCTET) {
			encoder.write(first | tag.number);
			return;
		}
		encoder.write(first | Tag.ONE_OCTET);
		int number = tag.number;
		byte[] buffer = new byte[5];
		int start = 4;
		buffer[start] = (byte)(number & SEVEN_BITS);
		for(number >>= 7; number > SEVEN_BITS; number >>= 7) {
			buffer[--start] = (byte)
				((number & SEVEN_BITS) | HIGH_BIT);
		}
		encoder.write(buffer, start, 5 - start);
	}

	/** Encode a BER length */
	protected void encodeLength(int length) throws IOException {
		if(length < 128) encoder.write(length);
		else if(length < 256) {
			encoder.write(HIGH_BIT | 1);
			encoder.write(length);
		}
		else {
			encoder.write(HIGH_BIT | 2);
			encoder.write((byte)(length >> 8));
			encoder.write((byte)(length & 0xFF));
		}
	}

	/** Encode a boolean value */
	protected void encodeBoolean(boolean value) throws IOException {
		encodeIdentifier(Tag.BOOLEAN);
		encodeLength(1);
		if(value) encoder.write(0xFF);
		else encoder.write(0x00);
	}

	/** Encode an integer value */
	protected void encodeInteger(int value) throws IOException {
		byte[] buffer = new byte[4];
		int len = 0;
		boolean flag = false;
		for(int shift = 23; shift > 0; shift -= 8) {
			int test = (value >> shift) & 0x1FF;
			if(test != 0 && test != 0x1FF) flag = true;
			if(flag) buffer[len++] = (byte)(test >> 1);
		}
		buffer[len++] = (byte)(value & 0xFF);
		encodeIdentifier(Tag.INTEGER);
		encodeLength(len);
		encoder.write(buffer, 0, len);
	}

	/** Encode an octet string */
	protected void encodeOctetString(byte[] string) throws IOException {
		encodeIdentifier(Tag.OCTET_STRING);
		encodeLength(string.length);
		encoder.write(string);
	}

	/** Encode a null value */
	protected void encodeNull() throws IOException {
		encodeIdentifier(Tag.NULL);
		encodeLength(0);
	}

	/** Encode an object identifier */
	protected void encodeObjectIdentifier(int[] oid) throws IOException {
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		bs.write(oid[0] * 40 + oid[1]);
		for(int i = 2; i < oid.length; i++) {
			int subid = oid[i];
			if(subid > SEVEN_BITS) {
				bs.write(HIGH_BIT | (subid >> 7));
				subid &= SEVEN_BITS;
			}
			bs.write(subid);
		}
		byte[] buffer = bs.toByteArray();
		encodeIdentifier(Tag.OBJECT_IDENTIFIER);
		encodeLength(buffer.length);
		encoder.write(buffer);
	}

	/** Encode a sequence (or sequence-of) */
	protected void encodeSequence(byte[] seq) throws IOException {
		encodeIdentifier(Tag.SEQUENCE);
		encodeLength(seq.length);
		encoder.write(seq);
	}

	/** Decode a BER identifier (tag) */
	protected Tag decodeIdentifier(InputStream is) throws IOException {
		int first = is.read();
		byte clazz = (byte)(first & Tag.CLASS_MASK);
		boolean constructed = (first & Tag.CONSTRUCTED) != 0;
		int number = (first & Tag.ONE_OCTET);
		if(number == Tag.ONE_OCTET) {
			number = 0;
			int next = HIGH_BIT;
			while((next & HIGH_BIT) != 0) {
				next = is.read();
				number <<= 7;
				number |= (next & SEVEN_BITS);
			}
		}
		return getTag(clazz, constructed, number);
	}

	/** Decode a BER length */
	protected int decodeLength(InputStream is) throws IOException {
		int first = is.read();
		if(first == RESERVED) throw new
			ParsingException("RESERVED LENGTH CODE");
		int length = first & SEVEN_BITS;
		if(length != first) {
			if(length == 0) throw new
				ParsingException("INDEFINITE LENGTH");
			int i = length;
			for(length = 0; i > 0; i--) {
				length <<= 8;
				length |= is.read();
			}
		}
		if(length > is.available()) throw new ParsingException(
			"INVALID LENGTH: " + length + " > " + is.available());
		return length;
	}

	/** Decode an integer */
	protected int decodeInteger(InputStream is) throws IOException {
		if(decodeIdentifier(is) != Tag.INTEGER) throw new
			ParsingException("EXPECTED AN INTEGER TAG");
		int length = decodeLength(is);
		if(length < 1 || length > 4) throw new
			ParsingException("INVALID INTEGER LENGTH");
		int value = (byte)is.read(); // NOTE: cast to preserve sign
		for(int i = 1; i < length; i++) {
			value <<= 8;
			value |= is.read();
		}
		return value;
	}

	/** Decode an octet string */
	protected byte[] decodeOctetString(InputStream is) throws IOException {
		if(decodeIdentifier(is) != Tag.OCTET_STRING) throw new
			ParsingException("EXPECTED OCTET STRING TAG");
		int length = decodeLength(is);
		if(length < 0) throw new
			ParsingException("NEGATIVE STRING LENGTH");
		byte[] buffer = new byte[length];
		if(length > 0 && is.read(buffer) != length) throw
			new ParsingException("FAILED TO READ STRING");
		return buffer;
	}

	/** Decode an object identifier */
	protected int[] decodeObjectIdentifier(InputStream is)
		throws IOException
	{
		if(decodeIdentifier(is) != Tag.OBJECT_IDENTIFIER) throw
			new ParsingException("EXPECTED OBJECT IDENTIFIER TAG");
		int length = decodeLength(is);
		if(length < 1) throw new
			ParsingException("NEGATIVE OID LENGTH");
		byte[] buffer = new byte[length];
		if(is.read(buffer) != length) throw new
			ParsingException("FAILED TO READ OID");
		// FIXME: this is obviously bogus
		return new int[0];
	}

	/** Decode a sequence (or sequence-of)
	  * @return Length of sequence */
	protected int decodeSequence(InputStream is) throws IOException {
		if(decodeIdentifier(is) != Tag.SEQUENCE) throw new
			ParsingException("EXPECTED SEQUENCE TAG");
		return decodeLength(is);
	}
}
