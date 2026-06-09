-- Corrige CPF do utilizador DEV lektecjava@gmail.com
UPDATE usuario
   SET cpf = '13188685483'
 WHERE lower(email) = 'lektecjava@gmail.com';
