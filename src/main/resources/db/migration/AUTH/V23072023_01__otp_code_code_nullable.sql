-- V23072023_01__otp_code_code_nullable.sql
ALTER TABLE otp_code
  MODIFY COLUMN code VARCHAR(32) NULL;
