
:: 1.生成Netty服务端私钥和证书仓库命令：
keytool -genkey -alias serverAlias -keysize 2048 -validity 36500 -keyalg RSA -dname "CN=localhost" -keypass serverKeyPassDemo -storepass serverStorePassDemo -keystore server.jks

:: 2.生成Netty服务端自签名证书：
keytool -export -alias serverAlias -keystore server.jks -storepass serverStorePassDemo -file server.cer

:: 3.生成客户端的密钥对和证书仓库：
keytool -genkey -alias clientAlias -keysize 2048 -validity 36500 -keyalg RSA -dname "CN=localhost" -keypass clientKeyPassDemo -storepass clientStorePassDemo -keystore client.jks

:: 4.将Netty服务端的证书导入到客户端的证书仓库：
keytool -import -trustcacerts -alias serverAlias -file server.cer -storepass clientStorePassDemo -keystore client.jks

:: 5.生成客户端的自签名证书
keytool -export -alias clientAlias -keystore client.jks -storepass clientStorePassDemo -file client.cer

:: 6.将客户端的自签名证书导入到服务端的信任证书仓库
keytool -import -trustcacerts -alias clientAlias -file client.cer -storepass serverStorePassDemo -keystore server.jks