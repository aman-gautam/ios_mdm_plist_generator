iOS MDM plist.encoded and plist.xml generator
=============================================


Disclaimer
----------

I do not own this code. I found it through a team mate who asked me for help in running this code. Since this code was unmaintained and not working on relatively newer versions of Java, I ported the program to Java 1.7 . The required dependencies are provided in the lib folder. They are also open source libraries. Since I am not going to maintain this code, I have directly included the jar to help the users avoid any problem with the new versions of the jars. Let me know if there is any license issue I am unaware of. 


Usage
-----

You should be able to directly import this project in Eclipse. After importing when you clean the project, Eclipse will create the bin folder for you.
If you cannot find bin folder in the eclipse, you may want to check out your file system using your favorite window explorer.


In the bin folder you will find com/softthinker/Test.class . In the same director copy your key files namely

* customer.der
* intermediate.pem
* mdm.pem
* root.pem
* vendor.pem


After this, you can directly run Test.java from eclipse and you will find the generated files in the root directory of your project.