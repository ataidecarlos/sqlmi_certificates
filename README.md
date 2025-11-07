# Azure SQL Managed Instance Certificate DNS Checker

This Java application connects to an Azure SQL Managed Instance and inspects the SSL certificate to display DNS names and other certificate information.

## Features

- Connects to Azure SQL Managed Instance via SSL/TLS
- Extracts and displays certificate information:
  - Subject and Issuer
  - Validity dates (NotBefore, NotAfter)
  - Thumbprint (SHA-1 fingerprint)
  - DNS names from Subject Alternative Names (SAN)
  - Serial number and signature algorithm

## Prerequisites

- Java 11 or higher
- Maven or Gradle (for dependency management)
- Network access to Azure SQL Managed Instance

## Building and Running

### Using Maven

```bash
# Compile the project
mvn clean compile

# Run the application
mvn exec:java
```

### Using Gradle

```bash
# Compile the project
./gradlew build

# Run the application
./gradlew run
```

### Using Java directly (without build tools)

```bash
# Download the JDBC driver manually
# https://docs.microsoft.com/en-us/sql/connect/jdbc/download-microsoft-jdbc-driver-for-sql-server

# Compile
javac -cp ".:mssql-jdbc-12.4.2.jre11.jar" CertificateDnsChecker.java

# Run
java -cp ".:mssql-jdbc-12.4.2.jre11.jar" CertificateDnsChecker
```

## Configuration

Edit the `CertificateDnsChecker.java` file to modify:

- **Server**: Update the `server` variable with your Managed Instance hostname
- **Port**: Default is 1433
- **Database**: Default is "master"

## Authentication (Optional)

If you want to actually connect to the database (not just inspect the certificate), uncomment one of the authentication options in the code:

1. **SQL Authentication**:
   ```java
   connectionUrl += "user=yourUsername;password=yourPassword;";
   ```

2. **Azure AD Password Authentication**:
   ```java
   connectionUrl += "authentication=ActiveDirectoryPassword;user=yourUsername;password=yourPassword;";
   ```

3. **Azure AD Integrated Authentication**:
   ```java
   connectionUrl += "authentication=ActiveDirectoryIntegrated;";
   ```

## Output

The application displays:
- Subject and Issuer information
- Certificate validity dates
- SHA-1 thumbprint
- All DNS names in the certificate's Subject Alternative Names
- Serial number and signature algorithm

## Notes

- The current implementation focuses on inspecting the SSL certificate
- Network connectivity to the specified server and port is required
- The certificate inspection works without database authentication
- For production use, implement proper error handling and authentication

## Comparison with PowerShell Script

This Java implementation provides the same functionality as the PowerShell script:
- Creates a TCP/SSL connection to the server
- Retrieves the SSL certificate
- Displays certificate details including DNS names from SAN
- Properly closes connections

The main difference is that Java uses the X509Certificate API instead of PowerShell's .NET classes.
