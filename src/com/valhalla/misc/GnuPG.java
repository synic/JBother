package com.valhalla.misc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import com.valhalla.jbother.BuddyList;
import com.valhalla.settings.Settings;

/**
 * A class that implements PGP interface for Java.
 * <P>
 * 
 * It calls gpg (GnuPG) program to do all the PGP commands. $Id:$
 * 
 * @author Yaniv Yemini, January 2004.
 * @author Based on a class GnuPG by John Anderson, which can be found
 * @author at:
 *         http://lists.gnupg.org/pipermail/gnupg-devel/2002-February/018098.html
 * @author modified for use in JBother by Andrey Zakirov, February 2005
 * @created March 9, 2005
 * @version 0.5.1
 * @see GnuPG - http://www.gnupg.org/
 */

public class GnuPG {

    // Constants:
    private final String kGnuPGCommand;

    private static final String kGnuPGArgs = " --batch --armor --output -";

    // Class vars:
    private int gpg_exitCode = -1;

    private String gpg_result;

    private String gpg_err;

    /**
     * Reads an output stream from an external process. Imeplemented as a thred.
     * 
     * @author synic
     * @created March 9, 2005
     */
    class ProcessStreamReader extends Thread {
        InputStream is;

        String type;

        OutputStream os;

        String fullLine = "";

        /**
         * Constructor for the ProcessStreamReader object
         * 
         * @param is
         *            Description of the Parameter
         * @param type
         *            Description of the Parameter
         */
        ProcessStreamReader(InputStream is, String type) {
            this(is, type, null);
        }

        /**
         * Constructor for the ProcessStreamReader object
         * 
         * @param is
         *            Description of the Parameter
         * @param type
         *            Description of the Parameter
         * @param redirect
         *            Description of the Parameter
         */
        ProcessStreamReader(InputStream is, String type, OutputStream redirect) {
            this.is = is;
            this.type = type;
            this.os = redirect;
        }

        /**
         * Main processing method for the ProcessStreamReader object
         */
        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null) {
                    fullLine = fullLine + line + "\n";
                }

            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        /**
         * Gets the string attribute of the ProcessStreamReader object
         * 
         * @return The string value
         */
        String getString() {
            return fullLine;
        }

    }

    /**
     * Sign
     * 
     * @param inStr
     *            input string to sign
     * @param secID
     *            ID of secret key to sign with
     * @param passPhrase
     *            passphrase for the secret key to sign with
     * @return true upon success
     */
    public boolean sign(String inStr, String secID, String passPhrase) {
        boolean success = false;
        File tmpFile = createTempFile(inStr);

        if (tmpFile != null) {
            success = runGnuPG("-u " + secID + " --passphrase-fd 0 -b "
                    + tmpFile.getAbsolutePath(), passPhrase);
            tmpFile.delete();
            if (success && this.gpg_exitCode != 0) {
                success = false;
            }
        }
        return success;
    }

    /**
     * ClearSign
     * 
     * @param inStr
     *            input string to sign
     * @param secID
     *            ID of secret key to sign with
     * @param passPhrase
     *            passphrase for the secret key to sign with
     * @return true upon success
     */
    public boolean clearSign(String inStr, String secID, String passPhrase) {
        boolean success = false;

        File tmpFile = createTempFile(inStr);

        if (tmpFile != null) {
            success = runGnuPG("-u " + secID
                    + " --passphrase-fd 0 --clearsign "
                    + tmpFile.getAbsolutePath(), passPhrase);
            tmpFile.delete();
            if (success && this.gpg_exitCode != 0) {
                success = false;
            }
        }
        return success;
    }

    /**
     * Signs and encrypts a string
     * 
     * @param inStr
     *            input string to encrypt
     * @param secID
     *            ID of secret key to sign with
     * @param keyID
     *            ID of public key to encrypt with
     * @param passPhrase
     *            passphrase for the secret key to sign with
     * @return true upon success
     */
    public boolean signAndEncrypt(String inStr, String secID, String keyID,
            String passPhrase) {
        boolean success = false;

        File tmpFile = createTempFile(inStr);

        if (tmpFile != null) {
            success = runGnuPG("-u " + secID + " -r " + keyID
                    + " --passphrase-fd 0 -se " + tmpFile.getAbsolutePath(),
                    passPhrase);
            tmpFile.delete();
            if (success && this.gpg_exitCode != 0) {
                success = false;
            }
        }
        return success;
    }

    /**
     * Encrypt
     * 
     * @param inStr
     *            input string to encrypt
     * @param secID
     *            ID of secret key to use
     * @param keyID
     *            ID of public key to encrypt with
     * @return true upon success
     */
    public boolean encrypt(String inStr, String secID, String keyID) {

        boolean success;
        success = runGnuPG("-u " + secID + " -r " + keyID + " --encrypt", inStr);
        if (success && this.gpg_exitCode != 0) {
            success = false;
        }
        return success;
    }

    /**
     * Decrypt
     * 
     * @param inStr
     *            input string to decrypt
     * @param passPhrase
     *            passphrase for the secret key to decrypt with
     * @return true upon success
     */
    public boolean decrypt(String inStr, String passPhrase) {
        boolean success = false;

        File tmpFile = createTempFile(inStr);

        if (tmpFile != null) {
            success = runGnuPG("--passphrase-fd 0 --decrypt "
                    + tmpFile.getAbsolutePath(), passPhrase);
            tmpFile.delete();
            if (success && this.gpg_exitCode != 0) {
                success = false;
            }
        }
        return success;
    }

    /**
     * List public keys in keyring
     * 
     * @param ID
     *            ID of public key to list, blank for all
     * @return true upon success
     */
    public boolean listKeys(String ID) {
        boolean success;
        success = runGnuPG("--list-keys --with-colons " + ID, null);
        if (success && this.gpg_exitCode != 0) {
            success = false;
        }
        return success;
    }

    /**
     * List secret keys in keyring
     * 
     * @param ID
     *            ID of secret key to list, blank for all
     * @return true upon success
     */
    public boolean listSecretKeys(String ID) {
        boolean success;
        success = runGnuPG("--list-secret-keys --with-colons " + ID, null);
        if (success && this.gpg_exitCode != 0) {
            success = false;
        }
        return success;
    }

    /**
     * Verify a signature
     * 
     * @param inStr
     *            signature to verify
     * @return true if verified.
     */
    public boolean verify(String signedString, String dataString) {
        boolean success = false;
        File signedFile = createTempFile(signedString);
        File dataFile = createTempFile(dataString);

        if ((signedFile != null) && (dataFile != null)) {
            success = runGnuPG("--verify " + signedFile.getAbsolutePath() + " "
                    + dataFile.getAbsolutePath(), null);
            signedFile.delete();
            dataFile.delete();
            if (success && this.gpg_exitCode != 0) {
                success = false;
            }
        }
        return success;
    }

    /**
     * Get processing result
     * 
     * @return result string.
     */
    public String getResult() {
        return gpg_result;
    }

    /**
     * Get error output from GnuPG process
     * 
     * @return error string.
     */
    public String getErrorString() {
        return gpg_err;
    }

    /**
     * Get GnuPG exit code
     * 
     * @return exit code.
     */
    public int getExitCode() {
        return gpg_exitCode;
    }

    /**
     * Runs GnuPG external program
     * 
     * @param commandArgs
     *            command line arguments
     * @param inputStr
     *            string to pass to GnuPG process
     * @return true if success.
     */
    private boolean runGnuPG(String commandArgs, String inputStr) {
        Process p;
        String fullCommand = kGnuPGCommand + " " + commandArgs;
        //		String fullCommand = commandArgs;

        try {
            p = Runtime.getRuntime().exec(fullCommand);
        } catch (IOException io) {
            System.out.println("io Error " + io.getMessage());
            com.valhalla.Logger.logException(io);
            return false;
        }
        if (inputStr != null) {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(p
                    .getOutputStream()));
            try {
                out.write(inputStr);
                out.close();
            } catch (IOException io) {
                System.out.println("Exception at write! " + io.getMessage());
                return false;
            }
        }

        ProcessStreamReader psr_stdout = new ProcessStreamReader(p
                .getInputStream(), "ERROR");
        ProcessStreamReader psr_stderr = new ProcessStreamReader(p
                .getErrorStream(), "OUTPUT");
        psr_stdout.start();
        psr_stderr.start();
        try {

            psr_stdout.join();
            psr_stderr.join();
        } catch (InterruptedException i) {
            System.out.println("Exception at join! " + i.getMessage());
            return false;
        }

        try {
            p.waitFor();

        } catch (InterruptedException i) {
            System.out.println("Exception at waitfor! " + i.getMessage());
            return false;
        }

        try {
            gpg_exitCode = p.exitValue();
        } catch (IllegalThreadStateException itse) {
            return false;
        }
        gpg_result = psr_stdout.getString();
        gpg_err = psr_stderr.getString();

        return true;
    }

    /**
     * A utility method for creating a unique temporary file when needed by one
     * of the main methods. <BR>
     * The file handle is store in tmpFile object var.
     * 
     * @param inStr
     *            data to write into the file.
     * @return true if success
     */
    private File createTempFile(String inStr) {
        File tmpFile = null;
        FileWriter fw;

        try {
            tmpFile = File.createTempFile("YGnuPG", null);
        } catch (Exception e) {
            System.out.println("Cannot create temp file " + e.getMessage());
            return null;
        }

        try {
            fw = new FileWriter(tmpFile);
            fw.write(inStr);
            fw.flush();
            fw.close();
        } catch (Exception e) {
            // delete our file:
            tmpFile.delete();

            System.out.println("Cannot write temp file " + e.getMessage());
            return null;
        }

        return tmpFile;
    }

    /**
     * Default constructor
     */
    public GnuPG() {
        kGnuPGCommand = Settings.getInstance().getProperty("gpgApplication",
                "gpg")
                + " " + kGnuPGArgs;
    }

    public GnuPG(String command) {
        kGnuPGCommand = command;
    }

    /**
     * Description of the Method
     * 
     * @param xEncryptedData
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public String decryptExtension(String xEncryptedData) {
        String gnupgPassword = BuddyList.getInstance().getGnuPGPassword();
        String encoding = null;
        xEncryptedData = xEncryptedData.replaceAll("(\n)+$", "");
        xEncryptedData = xEncryptedData.replaceAll("^(\n)+", "");
        if ((gnupgPassword != null)
                && decrypt("-----BEGIN PGP MESSAGE-----\nVersion: bla\n\n"
                        + xEncryptedData + "\n-----END PGP MESSAGE-----\n",
                        gnupgPassword)) {
            try {
                String systemEncoding = new String(getResult().getBytes(),
                        "UTF8");
                encoding = systemEncoding;
            } catch (java.io.UnsupportedEncodingException e) {
            }

        }
        return encoding.replaceAll("\n+$", "");
    }

    /**
     * Description of the Method
     * 
     * @param Data
     *            Description of the Parameter
     * @param gnupgSecretKey
     *            Description of the Parameter
     * @param gnupgPublicKey
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public String encryptExtension(String Data, String gnupgSecretKey,
            String gnupgPublicKey) {
        String encryptedData = null;
        try {
            byte[] utf8 = Data.getBytes("UTF8");
            String string = new String(utf8, MiscUtils.streamEncoding());
            Data = string;
        } catch (java.io.UnsupportedEncodingException e) {
        }

        if (encrypt(Data, gnupgSecretKey, gnupgPublicKey)) {
            encryptedData = getResult();
            encryptedData = encryptedData.replaceAll(
                    "-----BEGIN PGP MESSAGE-----(\n.*)+\n\n", "");
            encryptedData = encryptedData.replaceAll(
                    "\n-----END PGP MESSAGE-----\n", "");

        }
        return encryptedData;
    }

    /**
     * Description of the Method
     * 
     * @param Data
     *            Description of the Parameter
     * @param gnupgSecretKey
     *            Description of the Parameter
     * @param gnupgPublicKey
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public String signExtension(String Data, String gnupgSecretKey) {
        String gnupgPassword = BuddyList.getInstance().getGnuPGPassword();
        String signedData = null;
        try {
            byte[] utf8 = Data.getBytes("UTF8");
            String string = new String(utf8, MiscUtils.streamEncoding());
            Data = string;
        } catch (java.io.UnsupportedEncodingException e) {
        }

        if ((gnupgPassword != null)
                && (sign(Data, gnupgSecretKey, gnupgPassword))) {
            signedData = getResult();
            signedData = signedData.replaceAll(
                    "-----BEGIN PGP SIGNATURE-----(\n.*)+\n\n", "");
            signedData = signedData.replaceAll(
                    "\n-----END PGP SIGNATURE-----\n", "");
            signedData = signedData.replaceAll("^(\n)+", "");
            signedData = signedData.replaceAll("(\n)+$", "");
        }
        return signedData;
    }

    public String verifyExtension(String xSignedData, String messageBody) {
        String id = null;
        try {
            byte[] utf8 = messageBody.getBytes("UTF8");
            String string = new String(utf8, MiscUtils.streamEncoding());
            messageBody = string;
        } catch (java.io.UnsupportedEncodingException e) {
        }
        messageBody = messageBody.replaceAll("(\n)+$", "");
        xSignedData = xSignedData.replaceAll("(\n)+$", "");
        messageBody = messageBody.replaceAll("^(\n)+", "");
        xSignedData = xSignedData.replaceAll("^(\n)+", "");
        if (verify("-----BEGIN PGP SIGNATURE-----\nVersion: bla\n\n"
                + xSignedData + "\n-----END PGP SIGNATURE-----", messageBody)) {
            id = getErrorString();
            id = id.replaceAll(".*ID (.*)(\n.*)+", "$1");
        }
        return id;
    }

}

