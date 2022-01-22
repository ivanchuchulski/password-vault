INSERT INTO user(username, email, password, salt)
VALUES (?, ?, ?, ?);

INSERT INTO credential(username, website, site_username, password, salt, iv)
VALUES (?, ?, ?, ?, ?, ?);

UPDATE credential
SET password = ?, salt = ?, iv = ?
WHERE username = ? AND website = ? AND site_username = ?

SELECT password, salt, iv FROM credential WHERE username = ? AND  website = ? AND site_username = ?;