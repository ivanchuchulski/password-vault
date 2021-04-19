# PasswordVault
This is the project I created during the course "Modern Java technologies" in FMI Sofia. It is a client-servers password manager with console interface and it supports the following functionalities : 
- registration, login and logout
- for each logged-in user a session is maintained, which logs-out the user after certain time of no activity
- each registered and logged-in user can manually store username and password for specific site
- a REST service is used for generation of credentials for specified site and username
- a REST service is used to check the password strength for when storing credentials
- retrieval of credentials for specified site and username
- the credentaials are stored in JSON format, where the passwords for login are hashed and the passowords for website credentials are hashed