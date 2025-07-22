# BI4YOU - Système de Gestion des Utilisateurs Sécurisé

## Vue d'ensemble

BI4YOU est une application Spring Boot 3.5.3 conçue pour la gestion sécurisée des utilisateurs dans un environnement d'entreprise. Le système privilégie une approche centralisée où seuls les administrateurs peuvent créer des comptes utilisateurs, éliminant ainsi les risques liés à l'auto-inscription publique.

## Fonctionnalités Principales

### Sécurité Renforcée
- **Authentification JWT moderne** avec la bibliothèque jjwt 0.12.6
- **Encodage BCrypt** avec facteur de coût 12 pour un hachage robuste des mots de passe
- **Clés secrètes JWT** de 256 bits minimum pour une sécurité optimale
- **Gestion des tokens de réinitialisation** avec expiration automatique

### Gestion Administrative des Utilisateurs
- **Création d'utilisateurs par administrateur uniquement**
- **Génération automatique de mots de passe temporaires**
- **Envoi d'emails de bienvenue** avec identifiants de connexion
- **Activation/désactivation des comptes utilisateurs**
- **Réinitialisation de mots de passe par l'administrateur**

### Fonctionnalités Utilisateur
- **Gestion de profil personnel** (nom, prénom, email)
- **Changement de mot de passe sécurisé**
- **Réinitialisation de mot de passe par email**
- **Système de première connexion** obligeant le changement de mot de passe

## Architecture Technique

### Technologies Utilisées
- **Spring Boot 3.5.3** - Framework principal
- **Spring Security 6.x** - Sécurité et authentification
- **JWT (jjwt 0.12.6)** - Gestion des tokens d'authentification
- **MySQL** - Base de données relationnelle
- **Spring Data JPA** - Couche de persistance
- **Spring Mail** - Envoi d'emails
- **Lombok** - Réduction du code boilerplate
- **Maven** - Gestion des dépendances

### Structure du Projet
```
src/main/java/pi2425/bi4you/
├── controllers/          # Contrôleurs REST
│   ├── AdminController.java
│   ├── AuthController.java
│   └── UserController.java
├── dtos/                # Objets de transfert de données
│   ├── requests/
│   └── responses/
├── entities/            # Entités JPA
│   ├── User.java
│   ├── Roles.java
│   └── PasswordResetToken.java
├── repositories/        # Repositories JPA
├── security/           # Configuration de sécurité
│   ├── WebSecurityConfig.java
│   ├── jwt/
│   └── services/
└── services/           # Services métier
    ├── EmailService.java
    ├── impls/
    └── inters/
```

## Configuration et Installation

### Prérequis
- Java 17 ou supérieur
- MySQL 8.0 ou supérieur
- Maven 3.6 ou supérieur

### Configuration de la Base de Données
1. Créer une base de données MySQL nommée `BI4YOU`
2. Configurer les paramètres de connexion dans `application.properties`

### Configuration Email
Pour activer l'envoi d'emails, configurer les variables d'environnement :
```bash
export EMAIL_USERNAME=votre-email@gmail.com
export EMAIL_PASSWORD=votre-mot-de-passe-app
```

### Compilation et Exécution
```bash
# Compilation
mvn clean compile

# Exécution
mvn spring-boot:run
```

L'application sera accessible sur `http://localhost:3500`

## API Endpoints

### Authentification (`/api/auth`)

#### POST /api/auth/signin
Connexion utilisateur
```json
{
  "username": "admin",
  "password": "Admin123!"
}
```

#### POST /api/auth/forgetpassword
Demande de réinitialisation de mot de passe
```json
{
  "email": "user@example.com"
}
```

#### PUT /api/auth/resetpassword
Réinitialisation de mot de passe avec token
```json
{
  "token": "ABC12345",
  "password": "nouveauMotDePasse123!"
}
```

#### POST /api/auth/signout
Déconnexion utilisateur

### Administration (`/api/admin`) - Rôle ADMIN requis

#### GET /api/admin/users
Liste paginée des utilisateurs
- Paramètres : `page`, `size`, `sortBy`, `sortDir`

#### GET /api/admin/users/{id}
Détails d'un utilisateur spécifique

#### POST /api/admin/users
Création d'un nouvel utilisateur
```json
{
  "username": "john_doe",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "roles": ["DirecteurCommercial"],
  "sendWelcomeEmail": true
}
```

#### PUT /api/admin/users/{id}
Mise à jour d'un utilisateur
```json
{
  "email": "nouveau.email@example.com",
  "firstName": "Nouveau Prénom",
  "lastName": "Nouveau Nom",
  "roles": ["DirecteurMarketing"],
  "active": true
}
```

#### DELETE /api/admin/users/{id}
Suppression d'un utilisateur

#### PUT /api/admin/users/{id}/activate
Activation d'un compte utilisateur

#### PUT /api/admin/users/{id}/deactivate
Désactivation d'un compte utilisateur

#### POST /api/admin/users/{id}/reset-password
Réinitialisation du mot de passe par l'administrateur

#### GET /api/admin/roles
Liste de tous les rôles disponibles

### Gestion de Profil (`/api/user`) - Authentification requise

#### GET /api/user/profile
Récupération du profil de l'utilisateur connecté

#### PUT /api/user/profile
Mise à jour du profil utilisateur
```json
{
  "email": "nouveau.email@example.com",
  "firstName": "Nouveau Prénom",
  "lastName": "Nouveau Nom"
}
```

#### PUT /api/user/change-password
Changement de mot de passe
```json
{
  "currentPassword": "ancienMotDePasse",
  "newPassword": "nouveauMotDePasse123!",
  "confirmPassword": "nouveauMotDePasse123!"
}
```

## Rôles et Permissions

### Rôles Disponibles
- **ADMIN** - Accès complet à la gestion des utilisateurs
- **DirecteurGenerale** - Rôle de direction générale
- **DirecteurCommercial** - Rôle de direction commerciale
- **DirecteurMarketing** - Rôle de direction marketing
- **DirecteurAchats** - Rôle de direction des achats
- **ResponsableLogistique** - Rôle de responsable logistique

### Matrice des Permissions
| Endpoint | ADMIN | Autres Rôles |
|----------|-------|--------------|
| `/api/admin/**` | ✅ | ❌ |
| `/api/user/**` | ✅ | ✅ |
| `/api/auth/signin` | ✅ | ✅ |
| `/api/auth/forgetpassword` | ✅ | ✅ |
| `/api/auth/resetpassword` | ✅ | ✅ |

## Sécurité

### Mesures de Sécurité Implémentées
1. **Authentification JWT** avec tokens sécurisés
2. **Hachage BCrypt** des mots de passe avec coût élevé
3. **Validation des entrées** avec Bean Validation
4. **Protection CORS** configurée pour le frontend
5. **Gestion des sessions** en mode stateless
6. **Tokens de réinitialisation** avec expiration automatique
7. **Contrôle d'accès basé sur les rôles** (RBAC)

### Configuration de Sécurité
- **Clé secrète JWT** : 256 bits minimum
- **Expiration des tokens JWT** : 1 heure
- **Expiration des tokens de réinitialisation** : 24 heures
- **Facteur de coût BCrypt** : 12

## Utilisation

### Première Utilisation
1. Démarrer l'application
2. Se connecter avec le compte administrateur par défaut :
   - Username: `admin`
   - Password: `Admin123!`
3. Créer les utilisateurs nécessaires via l'interface d'administration
4. Les utilisateurs recevront leurs identifiants par email

### Workflow Typique
1. **Administrateur** crée un nouvel utilisateur
2. **Système** génère un mot de passe temporaire
3. **Email automatique** envoyé à l'utilisateur
4. **Utilisateur** se connecte avec les identifiants reçus
5. **Système** force le changement de mot de passe à la première connexion
6. **Utilisateur** peut ensuite gérer son profil et changer son mot de passe

## Améliorations Apportées

### Corrections de Sécurité
- ✅ Correction de l'encodage des mots de passe dans AuthController
- ✅ Mise à jour des dépendances JWT vers une version moderne
- ✅ Amélioration de la configuration de sécurité Spring
- ✅ Suppression de l'inscription publique

### Nouvelles Fonctionnalités
- ✅ Système de gestion des utilisateurs par administrateur
- ✅ Envoi d'emails automatiques
- ✅ Gestion des tokens de réinitialisation sécurisée
- ✅ Interface de gestion de profil utilisateur
- ✅ Pagination des listes d'utilisateurs
- ✅ Activation/désactivation des comptes

### Améliorations Techniques
- ✅ Refactorisation complète des services
- ✅ Ajout de validation robuste des données
- ✅ Gestion d'erreurs améliorée
- ✅ Documentation API complète
- ✅ Tests de compilation réussis

## Maintenance et Support

### Logs et Monitoring
L'application utilise SLF4J pour le logging. Les logs incluent :
- Tentatives de connexion
- Création/modification d'utilisateurs
- Envoi d'emails
- Erreurs de sécurité

### Sauvegarde
Assurer une sauvegarde régulière de :
- Base de données MySQL
- Configuration des emails
- Clés secrètes JWT

### Mise à Jour
Pour mettre à jour l'application :
1. Sauvegarder la base de données
2. Arrêter l'application
3. Déployer la nouvelle version
4. Redémarrer l'application
5. Vérifier les logs

## Contact et Support

Pour toute question ou support technique, contactez l'équipe de développement BI4YOU.

---

*Documentation générée par Manus AI - Version 1.0*

