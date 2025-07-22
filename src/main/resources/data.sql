-- Insertion des rôles par défaut
create table roles
(
    id bigint not null  auto_increment,
    name varchar(255) not null
);
INSERT IGNORE INTO roles (name) VALUES ('ADMIN');
INSERT IGNORE INTO roles (name) VALUES ('DirecteurGenerale');
INSERT IGNORE INTO roles (name) VALUES ('DirecteurCommercial');
INSERT IGNORE INTO roles (name) VALUES ('DirecteurAchats');
INSERT IGNORE INTO roles (name) VALUES ('ResponsableLogistique');
INSERT IGNORE INTO roles (name) VALUES ('DirecteurMarketing');

-- Création d'un utilisateur administrateur par défaut
-- Mot de passe: Admin123!
create table users
(
    username      varchar(255) not null,
    email         varchar(255) not null,
    password      varchar(255) not null,
    first_name    varchar(255) not null,
    last_name     varchar(255) not null,
    active        tinyint      not null,
    first_login   tinyint      not null,
    creation_date varchar(255) not null,
    id bigint NOT NULL AUTO_INCREMENT
);
INSERT IGNORE INTO users (username, email, password, first_name, last_name, active, first_login, creation_date)
VALUES ('admin', 'admin@bi4you.com', '$2a$12$8.UnVuG9HHgffUDAlmeX6.M4uOfzZc/1NcF2HJnqJqjrHBJHQ.aaa', 'Admin', 'System', true, false, NOW());

-- Associer le rôle ADMIN à l'utilisateur admin

-- Associer le rôle ADMIN à l'utilisateur admin
create table user_roles
(

    role_id int not null,
    user_id int not null
);
INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ADMIN';


