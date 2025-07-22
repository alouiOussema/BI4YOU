# Guide de Déploiement - BI4YOU

## Vue d'ensemble

Ce guide détaille les étapes nécessaires pour déployer l'application BI4YOU en environnement de production. L'application est conçue pour être déployée sur des serveurs Linux avec MySQL et peut être conteneurisée avec Docker.

## Prérequis Système

### Serveur de Production
- **OS**: Ubuntu 20.04 LTS ou supérieur / CentOS 8 ou supérieur
- **RAM**: Minimum 2GB, recommandé 4GB
- **CPU**: Minimum 2 cores
- **Stockage**: Minimum 20GB d'espace libre
- **Réseau**: Accès Internet pour les mises à jour et l'envoi d'emails

### Logiciels Requis
- **Java**: OpenJDK 17 ou supérieur
- **MySQL**: Version 8.0 ou supérieur
- **Maven**: Version 3.6 ou supérieur (pour la compilation)
- **Nginx**: Pour le reverse proxy (optionnel mais recommandé)

## Installation des Dépendances

### Ubuntu/Debian
```bash
# Mise à jour du système
sudo apt update && sudo apt upgrade -y

# Installation de Java 17
sudo apt install -y openjdk-17-jdk

# Installation de MySQL
sudo apt install -y mysql-server

# Installation de Maven (si compilation sur le serveur)
sudo apt install -y maven

# Installation de Nginx (optionnel)
sudo apt install -y nginx
```

### CentOS/RHEL
```bash
# Mise à jour du système
sudo yum update -y

# Installation de Java 17
sudo yum install -y java-17-openjdk-devel

# Installation de MySQL
sudo yum install -y mysql-server

# Installation de Maven
sudo yum install -y maven

# Installation de Nginx
sudo yum install -y nginx
```

## Configuration de la Base de Données

### 1. Sécurisation de MySQL
```bash
sudo mysql_secure_installation
```

### 2. Création de la Base de Données
```sql
-- Se connecter à MySQL en tant que root
mysql -u root -p

-- Créer la base de données
CREATE DATABASE BI4YOU CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Créer un utilisateur dédié
CREATE USER 'bi4you_user'@'localhost' IDENTIFIED BY 'motDePasseSecurise123!';

-- Accorder les privilèges
GRANT ALL PRIVILEGES ON BI4YOU.* TO 'bi4you_user'@'localhost';

-- Appliquer les changements
FLUSH PRIVILEGES;

-- Quitter MySQL
EXIT;
```

### 3. Configuration MySQL pour la Production
Éditer `/etc/mysql/mysql.conf.d/mysqld.cnf` :
```ini
[mysqld]
# Optimisations pour la production
innodb_buffer_pool_size = 1G
innodb_log_file_size = 256M
max_connections = 200
query_cache_size = 64M
query_cache_type = 1

# Sécurité
bind-address = 127.0.0.1
skip-networking = false
```

Redémarrer MySQL :
```bash
sudo systemctl restart mysql
sudo systemctl enable mysql
```

## Préparation de l'Application

### 1. Compilation de l'Application
```bash
# Cloner le repository (ou transférer les fichiers)
git clone https://github.com/alouiOussema/BI4YOU.git
cd BI4YOU

# Compilation
mvn clean package -DskipTests

# Le fichier JAR sera généré dans target/BI4YOU-0.0.1-SNAPSHOT.jar
```

### 2. Configuration de Production
Créer un fichier `application-prod.properties` :
```properties
# Configuration serveur
server.port=8080
server.servlet.context-path=/api

# Configuration base de données
spring.datasource.url=jdbc:mysql://localhost:3306/BI4YOU?createDatabaseIfNotExist=true&useSSL=true&serverTimezone=UTC
spring.datasource.username=bi4you_user
spring.datasource.password=motDePasseSecurise123!
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Configuration JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# Configuration sécurité
app.jwtSecret=VotreCleSecreteTresLongueEtSecurisePourLaProductionQuiDoitFaire256Bits
app.jwtExpirationMs=3600000
app.passwordResetTokenExpirationMs=86400000

# Configuration email
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${EMAIL_USERNAME}
spring.mail.password=${EMAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Configuration logging
logging.level.root=INFO
logging.level.pi2425.bi4you=DEBUG
logging.file.name=/var/log/bi4you/application.log
logging.file.max-size=10MB
logging.file.max-history=30
```

### 3. Variables d'Environnement
Créer un fichier `/etc/environment` ou `.env` :
```bash
EMAIL_USERNAME=votre-email@gmail.com
EMAIL_PASSWORD=votre-mot-de-passe-app
SPRING_PROFILES_ACTIVE=prod
```

## Déploiement

### 1. Création de l'Utilisateur Système
```bash
# Créer un utilisateur dédié
sudo useradd -r -s /bin/false bi4you

# Créer les répertoires
sudo mkdir -p /opt/bi4you
sudo mkdir -p /var/log/bi4you
sudo mkdir -p /etc/bi4you

# Copier l'application
sudo cp target/BI4YOU-0.0.1-SNAPSHOT.jar /opt/bi4you/bi4you.jar
sudo cp application-prod.properties /etc/bi4you/

# Définir les permissions
sudo chown -R bi4you:bi4you /opt/bi4you
sudo chown -R bi4you:bi4you /var/log/bi4you
sudo chown -R bi4you:bi4you /etc/bi4you
sudo chmod +x /opt/bi4you/bi4you.jar
```

### 2. Service Systemd
Créer `/etc/systemd/system/bi4you.service` :
```ini
[Unit]
Description=BI4YOU Application
After=network.target mysql.service
Requires=mysql.service

[Service]
Type=simple
User=bi4you
Group=bi4you
WorkingDirectory=/opt/bi4you
ExecStart=/usr/bin/java -jar -Dspring.config.location=/etc/bi4you/application-prod.properties -Dspring.profiles.active=prod /opt/bi4you/bi4you.jar
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=bi4you

# Sécurité
NoNewPrivileges=yes
PrivateTmp=yes
ProtectSystem=strict
ProtectHome=yes
ReadWritePaths=/var/log/bi4you

# Limites de ressources
LimitNOFILE=65536
LimitNPROC=4096

[Install]
WantedBy=multi-user.target
```

### 3. Activation du Service
```bash
# Recharger systemd
sudo systemctl daemon-reload

# Activer le service
sudo systemctl enable bi4you

# Démarrer le service
sudo systemctl start bi4you

# Vérifier le statut
sudo systemctl status bi4you

# Voir les logs
sudo journalctl -u bi4you -f
```

## Configuration Nginx (Reverse Proxy)

### 1. Configuration Nginx
Créer `/etc/nginx/sites-available/bi4you` :
```nginx
server {
    listen 80;
    server_name votre-domaine.com;

    # Redirection HTTPS (recommandé)
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name votre-domaine.com;

    # Certificats SSL (Let's Encrypt recommandé)
    ssl_certificate /etc/letsencrypt/live/votre-domaine.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/votre-domaine.com/privkey.pem;

    # Configuration SSL
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES256-GCM-SHA512:DHE-RSA-AES256-GCM-SHA512:ECDHE-RSA-AES256-GCM-SHA384:DHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;

    # Headers de sécurité
    add_header X-Frame-Options DENY;
    add_header X-Content-Type-Options nosniff;
    add_header X-XSS-Protection "1; mode=block";
    add_header Strict-Transport-Security "max-age=63072000; includeSubDomains; preload";

    # Proxy vers l'application
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # Timeouts
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # Logs
    access_log /var/log/nginx/bi4you_access.log;
    error_log /var/log/nginx/bi4you_error.log;
}
```

### 2. Activation de la Configuration
```bash
# Créer le lien symbolique
sudo ln -s /etc/nginx/sites-available/bi4you /etc/nginx/sites-enabled/

# Tester la configuration
sudo nginx -t

# Redémarrer Nginx
sudo systemctl restart nginx
sudo systemctl enable nginx
```

## Sécurisation

### 1. Firewall
```bash
# UFW (Ubuntu)
sudo ufw allow ssh
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable

# Ou iptables
sudo iptables -A INPUT -p tcp --dport 22 -j ACCEPT
sudo iptables -A INPUT -p tcp --dport 80 -j ACCEPT
sudo iptables -A INPUT -p tcp --dport 443 -j ACCEPT
sudo iptables -A INPUT -j DROP
```

### 2. Certificat SSL avec Let's Encrypt
```bash
# Installation de Certbot
sudo apt install -y certbot python3-certbot-nginx

# Obtention du certificat
sudo certbot --nginx -d votre-domaine.com

# Renouvellement automatique
sudo crontab -e
# Ajouter : 0 12 * * * /usr/bin/certbot renew --quiet
```

### 3. Monitoring et Logs
```bash
# Rotation des logs
sudo nano /etc/logrotate.d/bi4you
```

Contenu du fichier :
```
/var/log/bi4you/*.log {
    daily
    missingok
    rotate 52
    compress
    delaycompress
    notifempty
    create 644 bi4you bi4you
    postrotate
        systemctl reload bi4you
    endscript
}
```

## Sauvegarde

### 1. Script de Sauvegarde Base de Données
Créer `/opt/bi4you/backup.sh` :
```bash
#!/bin/bash

BACKUP_DIR="/opt/bi4you/backups"
DATE=$(date +%Y%m%d_%H%M%S)
DB_NAME="BI4YOU"
DB_USER="bi4you_user"
DB_PASS="motDePasseSecurise123!"

# Créer le répertoire de sauvegarde
mkdir -p $BACKUP_DIR

# Sauvegarde de la base de données
mysqldump -u $DB_USER -p$DB_PASS $DB_NAME > $BACKUP_DIR/bi4you_$DATE.sql

# Compression
gzip $BACKUP_DIR/bi4you_$DATE.sql

# Suppression des sauvegardes anciennes (> 30 jours)
find $BACKUP_DIR -name "*.sql.gz" -mtime +30 -delete

echo "Sauvegarde terminée: $BACKUP_DIR/bi4you_$DATE.sql.gz"
```

### 2. Automatisation avec Cron
```bash
sudo crontab -e
# Ajouter : 0 2 * * * /opt/bi4you/backup.sh
```

## Monitoring

### 1. Monitoring de l'Application
```bash
# Script de monitoring
cat > /opt/bi4you/monitor.sh << 'EOF'
#!/bin/bash

# Vérifier si l'application répond
if curl -f http://localhost:8080/api/auth/signin > /dev/null 2>&1; then
    echo "$(date): Application OK"
else
    echo "$(date): Application DOWN - Redémarrage"
    systemctl restart bi4you
fi
EOF

chmod +x /opt/bi4you/monitor.sh

# Ajouter au cron (toutes les 5 minutes)
sudo crontab -e
# Ajouter : */5 * * * * /opt/bi4you/monitor.sh >> /var/log/bi4you/monitor.log
```

### 2. Monitoring des Ressources
Installation de htop et iotop :
```bash
sudo apt install -y htop iotop
```

## Mise à Jour

### 1. Procédure de Mise à Jour
```bash
#!/bin/bash
# Script de mise à jour

# Arrêter l'application
sudo systemctl stop bi4you

# Sauvegarder la version actuelle
sudo cp /opt/bi4you/bi4you.jar /opt/bi4you/bi4you.jar.backup

# Sauvegarder la base de données
/opt/bi4you/backup.sh

# Déployer la nouvelle version
sudo cp target/BI4YOU-0.0.1-SNAPSHOT.jar /opt/bi4you/bi4you.jar
sudo chown bi4you:bi4you /opt/bi4you/bi4you.jar

# Redémarrer l'application
sudo systemctl start bi4you

# Vérifier le statut
sudo systemctl status bi4you
```

## Dépannage

### 1. Problèmes Courants

#### Application ne démarre pas
```bash
# Vérifier les logs
sudo journalctl -u bi4you -n 50

# Vérifier la configuration
java -jar /opt/bi4you/bi4you.jar --spring.config.location=/etc/bi4you/application-prod.properties --debug
```

#### Problèmes de base de données
```bash
# Vérifier la connexion MySQL
mysql -u bi4you_user -p -h localhost BI4YOU

# Vérifier les processus MySQL
sudo systemctl status mysql
```

#### Problèmes de mémoire
```bash
# Ajuster les paramètres JVM dans le service
ExecStart=/usr/bin/java -Xms512m -Xmx2g -jar ...
```

### 2. Logs Utiles
- Application: `/var/log/bi4you/application.log`
- Système: `sudo journalctl -u bi4you`
- Nginx: `/var/log/nginx/bi4you_*.log`
- MySQL: `/var/log/mysql/error.log`

## Performance

### 1. Optimisations JVM
```bash
# Dans le service systemd
ExecStart=/usr/bin/java \
  -Xms1g \
  -Xmx2g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+UseStringDeduplication \
  -jar /opt/bi4you/bi4you.jar
```

### 2. Optimisations MySQL
```sql
-- Analyser les performances
SHOW PROCESSLIST;
SHOW STATUS LIKE 'Slow_queries';

-- Optimiser les requêtes lentes
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 2;
```

## Conclusion

Ce guide couvre les aspects essentiels du déploiement de BI4YOU en production. Pour un environnement critique, considérez :

- Haute disponibilité avec plusieurs instances
- Load balancing
- Monitoring avancé (Prometheus, Grafana)
- Conteneurisation avec Docker/Kubernetes
- CI/CD avec Jenkins ou GitLab CI

---

*Guide de déploiement BI4YOU - Version 1.0*

