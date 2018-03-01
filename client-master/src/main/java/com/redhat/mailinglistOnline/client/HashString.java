package com.redhat.mailinglistOnline.client;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.log4j.Logger;


/**
 * This class is creating hashes from passwords, using PBKDF2WithHmacSHA1
 * 
 *@author Július Staššík
 */
public class HashString {
	
	final static Logger log = Logger.getLogger(HashString.class);
	
	public static String hash(String password, String saltInHex){
        int iterations = 1000;
        char[] chars = password.toCharArray();
        byte[] salt = null;
        if(saltInHex != null) {
        	salt = fromHex(saltInHex);
        } else {        
        	salt = getSalt();
        }
         
        PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 64 * 8);
        SecretKeyFactory skf = null;
		try {
			skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			byte[] hash;
			hash = skf.generateSecret(spec).getEncoded();
			return iterations + ":" + toHex(salt) + ":" + toHex(hash);
		} catch (NoSuchAlgorithmException e) {			
			log.error(e);		      
		} catch (InvalidKeySpecException e) {			
			log.error(e);
		}
        return null;
    }
     
	public static byte[] getSalt(){
        SecureRandom sr = null;
		try {
			sr = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {			
			e.printStackTrace();
		}
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return salt;
    }
     
	public static String toHex(byte[] array) {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if(paddingLength > 0)
        {
            return String.format("%0"  +paddingLength + "d", 0) + hex;
        }else{
            return hex;
        }
    }
	
	private static byte[] fromHex(String hex){
        byte[] bytes = new byte[hex.length() / 2];
        for(int i = 0; i<bytes.length ;i++)
        {
            bytes[i] = (byte)Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }

}
