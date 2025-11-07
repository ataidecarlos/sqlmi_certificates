import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLSession;
import javax.security.auth.x500.X500Principal;
import java.util.Collection;
import java.util.List;

public class CertificateDnsChecker {
    
    public static void main(String[] args) {

        String server = "xxxxxxxxxxxx.database.windows.net";
        String port = "1433";
        String database = "master"; // or your database name
        
        // Connection string for Azure SQL Managed Instance
        // Note: You'll need to provide authentication details
        String connectionUrl = String.format(
            "jdbc:sqlserver://%s:%s;database=%s;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;",
            server, port, database
        );
        
        Connection connection = null;
        
        try {
            // Load the JDBC driver
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            
            System.out.println("Connecting to: " + server);
            System.out.println("Port: " + port);
            System.out.println();
            
            // You'll need to add authentication to the connection string
            // Option 1: SQL Authentication
            // connectionUrl += "user=yourUsername;password=yourPassword;";
            
            // Option 2: Azure AD Authentication
            // connectionUrl += "authentication=ActiveDirectoryPassword;user=yourUsername;password=yourPassword;";
            
            // Option 3: Azure AD Integrated (requires additional setup)
            // connectionUrl += "authentication=ActiveDirectoryIntegrated;";
            
            // For this example, we'll use a custom trust manager to inspect the certificate
            // In production, you should use proper authentication
            
            // Create a custom connection to inspect SSL certificate
            inspectCertificate(server, Integer.parseInt(port));
            
        } catch (ClassNotFoundException e) {
            System.err.println("SQL Server JDBC Driver not found.");
            System.err.println("Add mssql-jdbc dependency to your project.");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error connecting to database:");
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private static void inspectCertificate(String server, int port) throws Exception {
        javax.net.ssl.SSLSocketFactory factory = 
            (javax.net.ssl.SSLSocketFactory) javax.net.ssl.SSLSocketFactory.getDefault();
        
        javax.net.ssl.SSLSocket socket = null;
        
        try {
            System.out.println("Creating SSL connection to inspect certificate...");
            socket = (javax.net.ssl.SSLSocket) factory.createSocket(server, port);
            socket.startHandshake();
            
            SSLSession session = socket.getSession();
            Certificate[] certificates = session.getPeerCertificates();
            
            if (certificates.length > 0 && certificates[0] instanceof X509Certificate) {
                X509Certificate cert = (X509Certificate) certificates[0];
                
                System.out.println("=== Certificate Information ===");
                System.out.println();
                
                // Subject
                X500Principal subject = cert.getSubjectX500Principal();
                System.out.println("Subject: " + subject.getName());
                
                // NotBefore and NotAfter
                System.out.println("NotBefore: " + cert.getNotBefore());
                System.out.println("NotAfter: " + cert.getNotAfter());
                
                // Thumbprint (SHA-1 fingerprint)
                byte[] encoded = cert.getEncoded();
                java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-1");
                byte[] digest = md.digest(encoded);
                System.out.println("Thumbprint (SHA-1): " + bytesToHex(digest));
                
                // Issuer
                X500Principal issuer = cert.getIssuerX500Principal();
                System.out.println("Issuer: " + issuer.getName());
                
                System.out.println();
                System.out.println("=== DNS Name List (Subject Alternative Names) ===");
                
                // DNS Names from Subject Alternative Names
                Collection<List<?>> sans = cert.getSubjectAlternativeNames();
                if (sans != null) {
                    for (List<?> san : sans) {
                        // SAN format: [type, value]
                        // Type 2 = DNS name
                        Integer type = (Integer) san.get(0);
                        if (type == 2) { // DNS name
                            String dnsName = (String) san.get(1);
                            System.out.println("  DNS Name: " + dnsName);
                        }
                    }
                } else {
                    System.out.println("  No Subject Alternative Names found");
                }
                
                System.out.println();
                System.out.println("=== Additional Certificate Details ===");
                System.out.println("Serial Number: " + cert.getSerialNumber());
                System.out.println("Signature Algorithm: " + cert.getSigAlgName());
                System.out.println("Version: " + cert.getVersion());
                
            } else {
                System.out.println("No X509 certificate found");
            }
            
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }
    
    // Helper method to convert bytes to hex string
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
