package com.dianping.pigeon.remoting.common.util;

import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.MacSpi;

public final class HmacSHA1 extends MacSpi
  implements Cloneable
{
  private HmacCore hmac = null;
  private static final int SHA1_BLOCK_LENGTH = 64;

  public HmacSHA1()
    throws NoSuchAlgorithmException
  {
    this.hmac = new HmacCore(MessageDigest.getInstance("SHA1"), 64);
  }

  protected int engineGetMacLength()
  {
    return this.hmac.getDigestLength();
  }

  protected void engineInit(Key paramKey, AlgorithmParameterSpec paramAlgorithmParameterSpec)
    throws InvalidKeyException, InvalidAlgorithmParameterException
  {
    this.hmac.init(paramKey, paramAlgorithmParameterSpec);
  }

  protected void engineUpdate(byte paramByte)
  {
    this.hmac.update(paramByte);
  }

  protected void engineUpdate(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
  {
    this.hmac.update(paramArrayOfByte, paramInt1, paramInt2);
  }

  protected void engineUpdate(ByteBuffer paramByteBuffer) {
    this.hmac.update(paramByteBuffer);
  }

  protected byte[] engineDoFinal()
  {
    return this.hmac.doFinal();
  }

  protected void engineReset()
  {
    this.hmac.reset();
  }

  public Object clone()
  {
    HmacSHA1 localHmacSHA1 = null;
    try {
      localHmacSHA1 = (HmacSHA1)super.clone();
      localHmacSHA1.hmac = ((HmacCore)this.hmac.clone());
    } catch (CloneNotSupportedException localCloneNotSupportedException) {
    }
    return localHmacSHA1;
  }
}
