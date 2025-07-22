# Collection Postman - BI4YOU API

## Configuration de Base

**Base URL:** `http://localhost:3500`

**Headers globaux:**
```
Content-Type: application/json
Accept: application/json
```

## Variables d'Environnement

Créer les variables suivantes dans Postman :
- `baseUrl`: `http://localhost:3500`
- `authToken`: (sera défini automatiquement après connexion)

## 1. Authentification

### 1.1 Connexion Administrateur
**POST** `{{baseUrl}}/api/auth/signin`

**Body (JSON):**
```json
{
  "username": "admin",
  "password": "Admin123!"
}
```

**Script Post-Response:**
```javascript
if (pm.response.code === 200) {
    const response = pm.response.json();
    pm.environment.set("authToken", response.token);
}
```

### 1.2 Connexion Utilisateur Standard
**POST** `{{baseUrl}}/api/auth/signin`

**Body (JSON):**
```json
{
  "username": "john_doe",
  "password": "motDePasseTemporaire"
}
```

### 1.3 Mot de Passe Oublié
**POST** `{{baseUrl}}/api/auth/forgetpassword`

**Body (JSON):**
```json
{
  "email": "user@example.com"
}
```

### 1.4 Réinitialisation de Mot de Passe
**PUT** `{{baseUrl}}/api/auth/resetpassword`

**Body (JSON):**
```json
{
  "token": "ABC12345",
  "password": "nouveauMotDePasse123!"
}
```

### 1.5 Déconnexion
**POST** `{{baseUrl}}/api/auth/signout`

**Headers:**
```
Authorization: Bearer {{authToken}}
```

## 2. Administration (Rôle ADMIN requis)

**Headers pour tous les endpoints admin:**
```
Authorization: Bearer {{authToken}}
Content-Type: application/json
```

### 2.1 Lister les Utilisateurs
**GET** `{{baseUrl}}/api/admin/users`

**Query Parameters:**
- `page`: 0 (optionnel)
- `size`: 10 (optionnel)
- `sortBy`: id (optionnel)
- `sortDir`: asc (optionnel)

### 2.2 Détails d'un Utilisateur
**GET** `{{baseUrl}}/api/admin/users/1`

### 2.3 Créer un Utilisateur
**POST** `{{baseUrl}}/api/admin/users`

**Body (JSON):**
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

### 2.4 Mettre à Jour un Utilisateur
**PUT** `{{baseUrl}}/api/admin/users/2`

**Body (JSON):**
```json
{
  "email": "nouveau.email@example.com",
  "firstName": "Nouveau Prénom",
  "lastName": "Nouveau Nom",
  "roles": ["DirecteurMarketing"],
  "active": true
}
```

### 2.5 Supprimer un Utilisateur
**DELETE** `{{baseUrl}}/api/admin/users/2`

### 2.6 Activer un Utilisateur
**PUT** `{{baseUrl}}/api/admin/users/2/activate`

### 2.7 Désactiver un Utilisateur
**PUT** `{{baseUrl}}/api/admin/users/2/deactivate`

### 2.8 Réinitialiser le Mot de Passe d'un Utilisateur
**POST** `{{baseUrl}}/api/admin/users/2/reset-password`

### 2.9 Lister les Rôles
**GET** `{{baseUrl}}/api/admin/roles`

## 3. Gestion de Profil Utilisateur

**Headers pour tous les endpoints utilisateur:**
```
Authorization: Bearer {{authToken}}
Content-Type: application/json
```

### 3.1 Récupérer son Profil
**GET** `{{baseUrl}}/api/user/profile`

### 3.2 Mettre à Jour son Profil
**PUT** `{{baseUrl}}/api/user/profile`

**Body (JSON):**
```json
{
  "email": "nouveau.email@example.com",
  "firstName": "Nouveau Prénom",
  "lastName": "Nouveau Nom"
}
```

### 3.3 Changer son Mot de Passe
**PUT** `{{baseUrl}}/api/user/change-password`

**Body (JSON):**
```json
{
  "currentPassword": "ancienMotDePasse",
  "newPassword": "nouveauMotDePasse123!",
  "confirmPassword": "nouveauMotDePasse123!"
}
```

## 4. Scénarios de Test

### Scénario 1: Workflow Complet Administrateur
1. Connexion admin
2. Créer un nouvel utilisateur
3. Lister les utilisateurs
4. Modifier l'utilisateur créé
5. Réinitialiser son mot de passe
6. Désactiver l'utilisateur
7. Réactiver l'utilisateur
8. Supprimer l'utilisateur

### Scénario 2: Workflow Utilisateur Standard
1. Connexion utilisateur
2. Récupérer son profil
3. Mettre à jour son profil
4. Changer son mot de passe
5. Déconnexion

### Scénario 3: Réinitialisation de Mot de Passe
1. Demander réinitialisation (mot de passe oublié)
2. Utiliser le token reçu par email
3. Définir un nouveau mot de passe
4. Se connecter avec le nouveau mot de passe

## 5. Tests de Sécurité

### 5.1 Test d'Accès Non Autorisé
**GET** `{{baseUrl}}/api/admin/users`

**Sans header Authorization** - Doit retourner 401

### 5.2 Test de Rôle Insuffisant
**GET** `{{baseUrl}}/api/admin/users`

**Avec token d'utilisateur non-admin** - Doit retourner 403

### 5.3 Test de Token Expiré
**GET** `{{baseUrl}}/api/user/profile`

**Avec token expiré** - Doit retourner 401

## 6. Réponses Attendues

### Connexion Réussie (200)
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "id": 1,
  "username": "admin",
  "email": "admin@bi4you.com",
  "roles": ["ROLE_ADMIN"]
}
```

### Utilisateur Créé (201)
```json
{
  "id": 2,
  "username": "john_doe",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "roles": ["DirecteurCommercial"],
  "active": true,
  "createdAt": "2025-07-20T16:00:00",
  "lastLogin": null
}
```

### Erreur de Validation (400)
```json
{
  "message": "Erreur: Le nom d'utilisateur existe déjà"
}
```

### Accès Non Autorisé (401)
```json
{
  "path": "/api/admin/users",
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "status": 401
}
```

### Accès Interdit (403)
```json
{
  "path": "/api/admin/users",
  "error": "Forbidden",
  "message": "Access Denied",
  "status": 403
}
```

## 7. Notes Importantes

### Authentification
- Tous les endpoints sauf `/api/auth/signin`, `/api/auth/forgetpassword` et `/api/auth/resetpassword` nécessitent un token JWT
- Le token doit être inclus dans le header `Authorization: Bearer <token>`
- Les tokens expirent après 1 heure

### Rôles
- Seuls les utilisateurs avec le rôle `ADMIN` peuvent accéder aux endpoints `/api/admin/**`
- Tous les utilisateurs authentifiés peuvent accéder aux endpoints `/api/user/**`

### Validation
- Les emails doivent avoir un format valide
- Les mots de passe doivent contenir au moins 6 caractères
- Les noms d'utilisateur doivent contenir entre 3 et 20 caractères

### Emails
- Les emails de bienvenue sont envoyés automatiquement lors de la création d'utilisateurs
- Les tokens de réinitialisation sont envoyés par email
- Configurer les paramètres SMTP pour activer l'envoi d'emails

---

*Collection Postman générée pour BI4YOU API - Version 1.0*

