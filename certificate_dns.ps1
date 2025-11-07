$server = "xxxxxxxxxxxx.database.windows.net"
$port = 1433
 
$tcpClient = New-Object Net.Sockets.TcpClient($server, $port)
$sslStream = New-Object Net.Security.SslStream($tcpClient.GetStream(), $false, ({ $True }))
$sslStream.AuthenticateAsClient($server)
$cert = New-Object System.Security.Cryptography.X509Certificates.X509Certificate2 $sslStream.RemoteCertificate
$cert | Format-List Subject, NotBefore, NotAfter, Thumbprint, FriendlyName, DnsNameList
$cert.DnsNameList

$cert.IssuerName

$sslStream.Close()
$tcpClient.Close()
