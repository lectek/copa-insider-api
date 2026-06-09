-- Grant access for app_user from any host so containers can connect
CREATE USER IF NOT EXISTS 'app_user'@'%' IDENTIFIED BY 'Rede_Mais_Farma25@';
ALTER USER 'app_user'@'%' IDENTIFIED WITH mysql_native_password BY 'Rede_Mais_Farma25@';
GRANT ALL PRIVILEGES ON redemaisfarma.* TO 'app_user'@'%';
FLUSH PRIVILEGES;
