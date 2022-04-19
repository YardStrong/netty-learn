
#生成Netty服务端私钥和证书仓库命令：
keytool -genkey -alias securechat -keysize 2048 -validity 365 -keyalg RSA -dname "CN=localhost" -keypass serverPassDemo -storepass serverPassDemo -keystore server.keytab

#生成Netty服务端自签名证书：
keytool -export -alias securechat -keystore server.keytab -storepass serverPassDemo -file server.cert

#生成客户端的密钥对和证书仓库：
keytool -genkey -alias smcc -keysize 2048 -validity 365 -keyalg RSA -dname "CN=localhost" -keypass clientPassDemo -storepass clientPassDemo -keystore client.keytab

#将Netty服务端的证书导入到客户端的证书仓库：
keytool -import -trustcacerts -alias securechat -file server.cert -storepass clientPassDemo -keystore client.keytab

# 生成客户端的自签名证书
keytool -export -alias smcc -keystore client.keytab -storepass clientPassDemo -file client.cert

#将客户端的自签名证书导入到服务端的信任证书仓库
keytool -import -trustcacerts -alias smcc -file client.cert -storepass serverPassDemo -keystore server.keytab