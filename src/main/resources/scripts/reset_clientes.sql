-- Resetea los campos telefono y direccion al estado del seed (V2)
UPDATE clientes SET telefono = NULL WHERE id % 3 = 1;
UPDATE clientes SET direccion = NULL WHERE id % 3 = 2;
UPDATE clientes SET telefono = NULL, direccion = NULL WHERE id % 3 = 0;
