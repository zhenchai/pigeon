package com.dianping.pigeon.remoting.common.util;

import java.nio.ByteBuffer;
import java.security.DigestException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.ProviderException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

import javax.crypto.MacSpi;
import javax.crypto.SecretKey;

final class HmacCore implements Cloneable {
	private final MessageDigest md;
	private final byte[] k_ipad;
	private final byte[] k_opad;
	private boolean first;
	private final int blockLen;

	HmacCore(MessageDigest paramMessageDigest, int paramInt) {
		this.md = paramMessageDigest;
		this.blockLen = paramInt;
		this.k_ipad = new byte[this.blockLen];
		this.k_opad = new byte[this.blockLen];
		this.first = true;
	}

	HmacCore(String paramString, int paramInt) throws NoSuchAlgorithmException {
		this(MessageDigest.getInstance(paramString), paramInt);
	}

	private HmacCore(HmacCore paramHmacCore) throws CloneNotSupportedException {
		this.md = ((MessageDigest) paramHmacCore.md.clone());
		this.blockLen = paramHmacCore.blockLen;
		this.k_ipad = ((byte[]) paramHmacCore.k_ipad.clone());
		this.k_opad = ((byte[]) paramHmacCore.k_opad.clone());
		this.first = paramHmacCore.first;
	}

	int getDigestLength() {
		return this.md.getDigestLength();
	}

	void init(Key paramKey, AlgorithmParameterSpec paramAlgorithmParameterSpec)
			throws InvalidKeyException, InvalidAlgorithmParameterException {
		if (paramAlgorithmParameterSpec != null) {
			throw new InvalidAlgorithmParameterException("HMAC does not use parameters");
		}

		if (!(paramKey instanceof SecretKey)) {
			throw new InvalidKeyException("Secret key expected");
		}

		byte[] localObject = paramKey.getEncoded();
		if (localObject == null) {
			throw new InvalidKeyException("Missing key data");
		}

		if (localObject.length > this.blockLen) {
			byte[] arrayOfByte = this.md.digest((byte[]) localObject);

			Arrays.fill((byte[]) localObject, (byte) 0);
			localObject = arrayOfByte;
		}

		for (int i = 0; i < this.blockLen; i++) {
			int j = i < localObject.length ? localObject[i] : 0;
			this.k_ipad[i] = ((byte) (j ^ 0x36));
			this.k_opad[i] = ((byte) (j ^ 0x5C));
		}

		Arrays.fill((byte[]) localObject, (byte) 0);
		localObject = null;

		reset();
	}

	void update(byte paramByte) {
		if (this.first == true) {
			this.md.update(this.k_ipad);
			this.first = false;
		}

		this.md.update(paramByte);
	}

	void update(byte[] paramArrayOfByte, int paramInt1, int paramInt2) {
		if (this.first == true) {
			this.md.update(this.k_ipad);
			this.first = false;
		}

		this.md.update(paramArrayOfByte, paramInt1, paramInt2);
	}

	void update(ByteBuffer paramByteBuffer) {
		if (this.first == true) {
			this.md.update(this.k_ipad);
			this.first = false;
		}

		this.md.update(paramByteBuffer);
	}

	byte[] doFinal() {
		if (this.first == true) {
			this.md.update(this.k_ipad);
		} else
			this.first = true;

		try {
			byte[] arrayOfByte = this.md.digest();

			this.md.update(this.k_opad);

			this.md.update(arrayOfByte);

			this.md.digest(arrayOfByte, 0, arrayOfByte.length);
			return arrayOfByte;
		} catch (DigestException localDigestException) {
			throw new ProviderException(localDigestException);
		}
	}

	void reset() {
		if (!this.first) {
			this.md.reset();
			this.first = true;
		}
	}

	public Object clone() throws CloneNotSupportedException {
		return new HmacCore(this);
	}

	public static final class HmacSHA512 extends MacSpi implements Cloneable {
		private final HmacCore core;

		public HmacSHA512() throws NoSuchAlgorithmException {
			this.core = new HmacCore("SHA-512", 128);
		}

		private HmacSHA512(HmacSHA512 paramHmacSHA512) throws CloneNotSupportedException {
			this.core = ((HmacCore) paramHmacSHA512.core.clone());
		}

		protected int engineGetMacLength() {
			return this.core.getDigestLength();
		}

		protected void engineInit(Key paramKey, AlgorithmParameterSpec paramAlgorithmParameterSpec)
				throws InvalidKeyException, InvalidAlgorithmParameterException {
			this.core.init(paramKey, paramAlgorithmParameterSpec);
		}

		protected void engineUpdate(byte paramByte) {
			this.core.update(paramByte);
		}

		protected void engineUpdate(byte[] paramArrayOfByte, int paramInt1, int paramInt2) {
			this.core.update(paramArrayOfByte, paramInt1, paramInt2);
		}

		protected void engineUpdate(ByteBuffer paramByteBuffer) {
			this.core.update(paramByteBuffer);
		}

		protected byte[] engineDoFinal() {
			return this.core.doFinal();
		}

		protected void engineReset() {
			this.core.reset();
		}

		public Object clone() throws CloneNotSupportedException {
			return new HmacSHA512(this);
		}
	}

	public static final class HmacSHA384 extends MacSpi implements Cloneable {
		private final HmacCore core;

		public HmacSHA384() throws NoSuchAlgorithmException {
			this.core = new HmacCore("SHA-384", 128);
		}

		private HmacSHA384(HmacSHA384 paramHmacSHA384) throws CloneNotSupportedException {
			this.core = ((HmacCore) paramHmacSHA384.core.clone());
		}

		protected int engineGetMacLength() {
			return this.core.getDigestLength();
		}

		protected void engineInit(Key paramKey, AlgorithmParameterSpec paramAlgorithmParameterSpec)
				throws InvalidKeyException, InvalidAlgorithmParameterException {
			this.core.init(paramKey, paramAlgorithmParameterSpec);
		}

		protected void engineUpdate(byte paramByte) {
			this.core.update(paramByte);
		}

		protected void engineUpdate(byte[] paramArrayOfByte, int paramInt1, int paramInt2) {
			this.core.update(paramArrayOfByte, paramInt1, paramInt2);
		}

		protected void engineUpdate(ByteBuffer paramByteBuffer) {
			this.core.update(paramByteBuffer);
		}

		protected byte[] engineDoFinal() {
			return this.core.doFinal();
		}

		protected void engineReset() {
			this.core.reset();
		}

		public Object clone() throws CloneNotSupportedException {
			return new HmacSHA384(this);
		}
	}

	public static final class HmacSHA256 extends MacSpi implements Cloneable {
		private final HmacCore core;

		public HmacSHA256() throws NoSuchAlgorithmException {
			this.core = new HmacCore("SHA-256", 64);
		}

		private HmacSHA256(HmacSHA256 paramHmacSHA256) throws CloneNotSupportedException {
			this.core = ((HmacCore) paramHmacSHA256.core.clone());
		}

		protected int engineGetMacLength() {
			return this.core.getDigestLength();
		}

		protected void engineInit(Key paramKey, AlgorithmParameterSpec paramAlgorithmParameterSpec)
				throws InvalidKeyException, InvalidAlgorithmParameterException {
			this.core.init(paramKey, paramAlgorithmParameterSpec);
		}

		protected void engineUpdate(byte paramByte) {
			this.core.update(paramByte);
		}

		protected void engineUpdate(byte[] paramArrayOfByte, int paramInt1, int paramInt2) {
			this.core.update(paramArrayOfByte, paramInt1, paramInt2);
		}

		protected void engineUpdate(ByteBuffer paramByteBuffer) {
			this.core.update(paramByteBuffer);
		}

		protected byte[] engineDoFinal() {
			return this.core.doFinal();
		}

		protected void engineReset() {
			this.core.reset();
		}

		public Object clone() throws CloneNotSupportedException {
			return new HmacSHA256(this);
		}
	}
}
