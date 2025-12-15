create DATABASE framework;

use framework;

create table etudiant(
    id int AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100),
    prenom VARCHAR(100),
    dateNaissance DATE,
    address VARCHAR(100),
    numero VARCHAR(100)
);