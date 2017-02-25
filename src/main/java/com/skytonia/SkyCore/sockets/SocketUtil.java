package com.skytonia.SkyCore.sockets;

import com.google.gson.Gson;
import com.skytonia.SkyCore.util.BUtil;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

public class SocketUtil
{
	
	public static Gson gson = new Gson();
	
	public static Gson gson()
	{
		return gson;
	}
	
	public interface SocketLogger
	{
		
		void info(String str);
		
		void warning(String str);
		
	}
	
	public static String encrypt(String data, String pass)
	{
		try
		{
			while(pass.length() < 24)
			{
				pass += pass;
			}
			SecretKeyFactory factory = SecretKeyFactory.getInstance("DESede");
			SecretKey key = factory.generateSecret(new DESedeKeySpec(pass.getBytes()));
			Cipher cipher = Cipher.getInstance("DESede");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			String str = DatatypeConverter.printBase64Binary(cipher.doFinal(data.getBytes()));
			return str;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public static String decrypt(String data, String pass)
	{
		try
		{
			while(pass.length() < 24)
			{
				pass += pass;
			}
			SecretKeyFactory factory = SecretKeyFactory.getInstance("DESede");
			SecretKey key = factory.generateSecret(new DESedeKeySpec(pass.getBytes()));
			Cipher cipher = Cipher.getInstance("DESede");
			cipher.init(Cipher.DECRYPT_MODE, key);
			return new String(cipher.doFinal(DatatypeConverter.parseBase64Binary(data)));
		}
		catch(Exception e)
		{
			//
		}
		return null;
	}
	
	public static String[] split(String input, int max)
	{
		if(input == null)
		{
			BUtil.logInfo("Attempted to split null string");
			//return new String[] {""};
			throw new IllegalArgumentException("");
		}
		
		return input.split("(?<=\\G.{" + max + "})");
	}
	
	public static class RSA
	{
		
		public static KeyPair generateKeys()
		{
			try
			{
				return KeyPairGenerator.getInstance("RSA").generateKeyPair();
			}
			catch(NoSuchAlgorithmException e)
			{
				e.printStackTrace();
			}
			return null;
		}
		
		public static String encrypt(String data, PublicKey key)
		{
			try
			{
				Cipher rsa = Cipher.getInstance("RSA");
				rsa.init(Cipher.ENCRYPT_MODE, key);
				return DatatypeConverter.printBase64Binary(rsa.doFinal(data.getBytes()));
			}
			catch(NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e)
			{
				e.printStackTrace();
			}
			catch(InvalidKeyException e)
			{
			}
			return null;
		}
		
		public static String decrypt(String data, PrivateKey key)
		{
			try
			{
				Cipher rsa = Cipher.getInstance("RSA");
				rsa.init(Cipher.DECRYPT_MODE, key);
				return new String(rsa.doFinal(DatatypeConverter.parseBase64Binary(data)));
			}
			catch(NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e)
			{
				e.printStackTrace();
			}
			catch(InvalidKeyException e)
			{
			}
			return null;
		}
		
		public static PrivateKey loadPrivateKey(String key64) throws GeneralSecurityException, IOException
		{
			byte[] clear = Base64.getDecoder().decode(key64);
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(clear);
			KeyFactory fact = KeyFactory.getInstance("RSA");
			PrivateKey priv = fact.generatePrivate(keySpec);
			Arrays.fill(clear, (byte) 0);
			return priv;
		}
		
		public static PublicKey loadPublicKey(String stored) throws GeneralSecurityException, IOException
		{
			byte[] data = Base64.getDecoder().decode(stored);
			X509EncodedKeySpec spec = new X509EncodedKeySpec(data);
			KeyFactory fact = KeyFactory.getInstance("RSA");
			return fact.generatePublic(spec);
		}
		
		public static String savePrivateKey(PrivateKey priv) throws GeneralSecurityException
		{
			KeyFactory fact = KeyFactory.getInstance("RSA");
			PKCS8EncodedKeySpec spec = fact.getKeySpec(priv, PKCS8EncodedKeySpec.class);
			byte[] packed = spec.getEncoded();
			String key64 = Base64.getEncoder().encodeToString(packed);
			Arrays.fill(packed, (byte) 0);
			return key64;
		}
		
		public static String savePublicKey(PublicKey publ) throws GeneralSecurityException
		{
			KeyFactory fact = KeyFactory.getInstance("RSA");
			X509EncodedKeySpec spec = fact.getKeySpec(publ, X509EncodedKeySpec.class);
			return Base64.getEncoder().encodeToString(spec.getEncoded());
		}
	}

}
