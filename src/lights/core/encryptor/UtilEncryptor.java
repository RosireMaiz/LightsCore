package lights.core.encryptor;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

public class UtilEncryptor {
	
	private static final String password = "lights_core_pass_x1";
	
	public static String encriptar(String cadena) { 
        StandardPBEStringEncryptor s = new StandardPBEStringEncryptor(); 
        
        s.setPassword(password); 
        return s.encrypt(cadena); 
    } 
 
    public static String desencriptar(String cadena) { 
        StandardPBEStringEncryptor s = new StandardPBEStringEncryptor(); 
        
        s.setPassword(password); 
        
        try { 
            return s.decrypt(cadena); 
        } catch (Exception e) { 
        	return "";
        } 
         
    } 
}
