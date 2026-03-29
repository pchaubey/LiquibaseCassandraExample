#!/bin/bash

# ============================
# CONFIG
# ============================
PASSWORD="changeit"
VALIDITY_DAYS=3650
CA_ALIAS="myRootCA"
SERVER_ALIAS="identity"
SERVER_KEYSTORE="identity.p12"
TRUSTSTORE="http-trust.p12"
CA_KEY="ca-key.pem"
CA_CERT="ca-cert.pem"
SERVER_CSR="identity.csr"
SERVER_CERT="identity-cert.pem"

# ============================
# 1. Create ROOT CA (private key + self-signed certificate)
# ============================
echo "==> Generating ROOT CA"
openssl req -x509 -newkey rsa:4096 \
  -sha256 -days $VALIDITY_DAYS \
  -nodes \
  -keyout $CA_KEY \
  -out $CA_CERT \
  -subj "/CN=MyDevRootCA/O=MyCompany/C=NL"

# ============================
# 2. Generate SERVER keypair and CSR (certificate signing request)
# ============================
echo "==> Generating server private key + CSR"
openssl req -newkey rsa:4096 \
  -nodes \
  -keyout server-key.pem \
  -out $SERVER_CSR \
  -subj "/CN=localhost/O=MyCompany/C=NL"

# ============================
# 3. Sign server certificate with ROOT CA
# ============================
echo "==> Signing server certificate with Root CA"
openssl x509 -req \
  -in $SERVER_CSR \
  -CA $CA_CERT -CAkey $CA_KEY \
  -CAcreateserial \
  -out $SERVER_CERT \
  -days $VALIDITY_DAYS \
  -sha256

# ============================
# 4. Create PKCS12 keystore (server-key + server-cert + Root CA)
# ============================
echo "==> Creating server keystore ($SERVER_KEYSTORE)"
openssl pkcs12 -export \
  -in $SERVER_CERT \
  -inkey server-key.pem \
  -certfile $CA_CERT \
  -name $SERVER_ALIAS \
  -out $SERVER_KEYSTORE \
  -passout pass:$PASSWORD

# ============================
# 5. Create truststore (contains only Root CA)
# ============================
echo "==> Creating truststore ($TRUSTSTORE)"
keytool -importcert \
  -alias $CA_ALIAS \
  -file $CA_CERT \
  -keystore $TRUSTSTORE \
  -storepass $PASSWORD \
  -noprompt \
  -storetype PKCS12

# ============================
# 6. Convert PKCS12 keystore to JKS keystore
# ============================
IDENTITY_JKS="identity.jks"
echo "==> Converting $SERVER_KEYSTORE to $IDENTITY_JKS (JKS format)"
keytool -importkeystore \
  -srckeystore $SERVER_KEYSTORE \
  -srcstoretype PKCS12 \
  -srcstorepass $PASSWORD \
  -destkeystore $IDENTITY_JKS \
  -deststoretype JKS \
  -deststorepass $PASSWORD

# ============================
# 7. Convert PKCS12 truststore to JKS truststore (if http-trust.p12 exists)
# ============================
HTTP_TRUST_P12="http-trust.p12"
HTTP_TRUST_JKS="http-trust.jks"
if [ -f "$HTTP_TRUST_P12" ]; then
  echo "==> Converting $HTTP_TRUST_P12 to $HTTP_TRUST_JKS (JKS format)"
  keytool -importkeystore \
    -srckeystore $HTTP_TRUST_P12 \
    -srcstoretype PKCS12 \
    -srcstorepass $PASSWORD \
    -destkeystore $HTTP_TRUST_JKS \
    -deststoretype JKS \
    -deststorepass $PASSWORD
else
  echo "[INFO] $HTTP_TRUST_P12 not found, skipping JKS conversion for truststore."
fi

echo "✅ DONE"
echo "Generated files:"
echo "  - $CA_KEY (Root CA private key)"
echo "  - $CA_CERT (Root CA certificate)"
echo "  - server-key.pem (Server private key)"
echo "  - server-cert.pem (Server certificate signed by CA)"
echo "  - $SERVER_KEYSTORE (PKCS12 keystore)"
echo "  - $TRUSTSTORE (PKCS12 truststore)"
echo "  - $IDENTITY_JKS (JKS keystore)"
echo "  - $HTTP_TRUST_JKS (JKS truststore, if http-trust.p12 exists)"
