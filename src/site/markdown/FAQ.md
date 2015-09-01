* Q. Why ```JCUnit``` isn't using ```GSON``` or other libraries to render JSON representations
of test cases?
A. Because we think a library like ```JCUnit```, which can be used for testings of any
 sort of software, should depend on any other libraries as less as possible.

* Q. Why ```JCUnit``` doesn't use Java 7 (or later) features and stick to being Java 6 compliant?
A. In order to use it for Java 6 based applications. Although it's public update isn't available anymore,
a lot of software products have been developed for it and there would still be a running with it.
 The author of JCUnit thought that they probably want to use it in the same environment as the one where 
 they are running their SUT.  
