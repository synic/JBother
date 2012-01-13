Per GNU GENERAL PUBLIC LICENSE, here are the modifications to the source code
for this version of JBother (kiosk mode).

This patch is made to run against source code for JBother version 0.8.6, and will add just enough code
to allow starting a JBother client using a pre-authenticated user alias to enter a predetermined
mu-conf group chat room.  This code is not perfect, additional optimizations are needed, but hey, it works.

Requirements for this example:
* A jabberd2 jabber server daemon using a MySQL back end ( http://jabberd.jabberstudio.org/2/ , http://www.mysql.com )
* phpBB on a PHP-enabled web server ( http://www.phpbb.com )

Additional authentication mechanisms can be used, and other jabber servers can be used, the
important steps are the ability to dynamically create a jabber login and access to the variables
which describe the user you want to create.  In this example for phpBB and Jabberd version 2,
the JBother app is launched to a group chat room with the same nickname as the phpBB login.  The
SQL handling in phpBB are used to create the jabber user, but other functions could be used.

Known bugs:

* Enough obfuscation is applied to prevent the created login from being reused without intercepting
the transmitted JNLP file, however a normal jabber client (including an unrestricted non-kiosk
mode JBother client) could still be used to enter the room after the password is viewed from
the JNLP file.  This can be solved with additional coding.
* I think the Jabber server must allow plain text authentication, as not all of the columns for the
dynamically created jabber user are populated... not sure about this.

Steps to apply this patch:
* move the included jbother directory to the directory at the same level as the phpBB installation
* move the JBother-0.8.6__kiosk-mode.patch file to an unpacked JBother 0.8.6 source directory
* change to that unpacked JBother 0.8.6 source directory
* add the patch to the JBother source code with the command:
	patch -p1 < JBother-0.8.6__kiosk-mode.patch
	(If you're doing this on Windows, I suggest using the cygwin patch command)
* repair any errors that might have occurred
* compile the code and copy the JAR file to the jbother directory
* sign the jar file! Using Sun Java, the commands are:
	- generate a certificate:
	keytool -genkey -keyalg RSA -keysize 1024 -validity 3648 -keystore keystore.bin -alias JBother
	- sign the jar:
	jarsigner -keystore keystore.bin JBother.jar JBother
	- remove the certificate (or move it out of the web space)
	rm -f keystore.bin
* at this point, the jbother directory should have the jar file, the jnlp template,
	the launch PHP file, and an empty jnlp directory. The jnlp directory should be made writable
	by the web service process owner (apache)
* edit the launch PHP script and the jnlp template to have the correct domain name,
	database parameters, and other parameters desired.
* try it out.  Ensure the jabber database is accessed correctly, the user is being created, the JNLP file is
	being created, add prints to the PHP file if there's any trouble.

Many gracious thanks to Adam Olsen for his assistance and for providing such a fantastic application!

-- Roger Venable (visualecho@yahoo.com) http://www.mdve.net

