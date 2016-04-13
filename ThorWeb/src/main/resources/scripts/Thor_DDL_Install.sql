/* Table to store user credentials for OrcId OAuth */
CREATE TABLE THOR_CREDENTIAL
( sessionId varchar2(50) PRIMARY KEY NOT NULL,
  accessToken varchar2(50) UNIQUE USING INDEX NOT NULL, 
  refreshToken varchar2(50),
  tokenServerUrl varchar2(250),
  orcId varchar2(50),
  scope varchar2(4000),
  expirationTimeMilliseconds NUMBER(19,0), 
  creationDate TIMESTAMP NOT NULL
) ENABLE PRIMARY KEY USING INDEX;