package com.softhinker;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

//import Decoder.BASE64Encoder;
import org.apache.commons.codec.binary.Base64;;

/**
 * This is a old code ported to work with java 1.7.
 * 
 * This class is to generate encoded plist for iOS MDM signing request.
 * Below files should be in the folder :
 *  - customer.der
 *  - intermediate.pem
 *  - mdm.pem
 *  - root.pem
 *  - vendor.p12
 *
 * Then upload 'plist_encoded' to https://identity.apple.com/pushcert/ to
 * generate the certificate for your customer.
 *
 * [Author Introduction]
 * Softhinker.com is a Singapore-based independent software vendor,
 * focusing on J2EE, Android, iOS, Google Apps development and consultancy.
 * Please visit us at http://www.softhinker.com for more details.
 *
 * @author Softhinker
 *
 */

public class Test {
	/*
	 * Configure the following.
	 */
	private static final String ALIAS = "MY_ALIAS";
	private static final String PASSWORD = "MY_PASSWORD";
	
	
	private static final String VENDOR_P12 = "vendor.p12";
	private static final String CUSTOMER_DER = "customer.der";
	private static final String MDM_PEM = "mdm.pem";
	private static final String INTERMEDIATE_PEM = "intermediate.pem";
	private static final String ROOT_PEM = "root.pem";

    public static void main(String[] args) throws Exception {

        URL dirUrl = Test.class.getResource(".");
        URL keyUrl = new URL(dirUrl, VENDOR_P12);
        String keyPath = keyUrl.getPath().replaceAll("%20", " ");

        Test test = new Test();
        PrivateKey privateKey = test.extractPrivateKey(keyPath);
        URL csrUrl = new URL(dirUrl, CUSTOMER_DER);
        
        String csrPath = csrUrl.getPath().replace("%20", " ");
        byte[] csrBytes = test.readCSR(csrPath);
        String csr = Base64.encodeBase64String(csrBytes);
//        String csr = b64en.encode(csrBytes);
        byte[] sigBytes = test.signCSR(privateKey, csrBytes);
//        String signature = b64en.encode(sigBytes);
        String signature = Base64.encodeBase64String(sigBytes);

        URL mdmUrl = new URL(dirUrl, MDM_PEM);
        String mdmPath = mdmUrl.getPath().replace("%20", " ");
        String mdm = test.readCertChain(mdmPath);

        URL intermediateUrl = new URL(dirUrl, INTERMEDIATE_PEM);
        String intermediatePath = intermediateUrl.getPath().replace("%20", " ");
        String intermediate = test.readCertChain(intermediatePath);
        URL rootUrl = new URL(dirUrl, ROOT_PEM);
        String rootPath = rootUrl.getPath().replace("%20", " ");
        String root = test.readCertChain(rootPath);
        StringBuffer sb = new StringBuffer();
        sb.append(mdm);
        sb.append(intermediate);
        sb.append(root);
        test.generatePlist(csr, sb.toString(), signature);
    }



    private byte[] signCSR(PrivateKey privateKey, byte[] csr) throws Exception {
        Signature sig = Signature.getInstance("SHA1WithRSA");
        sig.initSign(privateKey);
        sig.update(csr);
        byte[] signatureBytes = sig.sign();
        return signatureBytes;
    }



    private PrivateKey extractPrivateKey(String path2keystore) 
    		throws KeyStoreException, NoSuchAlgorithmException, CertificateException,
    			FileNotFoundException, IOException, UnrecoverableKeyException{

    	String alias = ALIAS;//Change to your alias
        String password = PASSWORD ;//Change to your password
        KeyStore caKs = KeyStore.getInstance("PKCS12");
        caKs.load(new FileInputStream(new File(path2keystore)), password.toCharArray());
        Key key = caKs.getKey(alias, password.toCharArray());
        return (PrivateKey)key;
    }

    private byte[] readCSR(String path2csr) throws IOException{
        FileInputStream fis = new FileInputStream(path2csr);
        byte[] csrBytes = new byte[fis.available()];
        fis.read(csrBytes);
        fis.close();
        return csrBytes;
    }

    private String readCertChain(String path2certchain) throws IOException{
        FileInputStream fis = new FileInputStream(path2certchain);
        byte[] csrBytes = new byte[fis.available()];
        fis.read(csrBytes);
        fis.close();
        return new String(csrBytes);
    }

    private void generatePlist(String csr, String chain, String signature) throws IOException{
        Document document = DocumentHelper.createDocument();
        document.addDocType("plist", "-//Apple//DTD PLIST 1.0//EN", "http://www.apple.com/DTDs/PropertyList-1.0.dtd");
        Element plist = document.addElement("plist");
        plist.addAttribute("version", "1.0");
        Element dict = plist.addElement("dict");
        Element csrKey = dict.addElement("key");
        csrKey.addText("PushCertRequestCSR");
        Element csrStr = dict.addElement("string");
        csrStr.addText(csr);
        Element chainKey = dict.addElement("key");
        chainKey.addText("PushCertCertificateChain");
        Element chainStr = dict.addElement("string");
        chainStr.addText(chain);
        Element sigKey = dict.addElement("key");
        sigKey.addText("PushCertSignature");
        Element sigStr = dict.addElement("string");
        sigStr.addText(signature);
        String plistxml = document.asXML();
//        BASE64Encoder b64en = new BASE64Encoder();
//        String encodedplist = b64en.encode(plistxml.getBytes());
        String encodedplist = Base64.encodeBase64String(plistxml.getBytes());
        FileWriter writer = new FileWriter("plist.xml");
        document.write(writer);
        writer.flush();
        writer.close();
        FileWriter out = new FileWriter("plist_encoded");
        out.write(encodedplist);
        out.flush();
        out.close();
        System.out.println("File is generated.");
    }
}