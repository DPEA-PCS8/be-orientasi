-- Drop old check constraint if exists and add new one with updated status values
-- This runs on application startup

-- Try to drop the constraint (will fail silently if doesn't exist in some configs)
IF EXISTS (SELECT * FROM sys.check_constraints WHERE name = 'CK__trn_pksi___statu__7F2BE32F')
BEGIN
    ALTER TABLE trn_pksi_document DROP CONSTRAINT CK__trn_pksi___statu__7F2BE32F;
END

-- Also drop any other status constraints that might exist
DECLARE @constraintName NVARCHAR(200)
SELECT @constraintName = name FROM sys.check_constraints 
WHERE parent_object_id = OBJECT_ID('trn_pksi_document') 
AND name LIKE '%statu%'

IF @constraintName IS NOT NULL
BEGIN
    EXEC('ALTER TABLE trn_pksi_document DROP CONSTRAINT ' + @constraintName)
END
