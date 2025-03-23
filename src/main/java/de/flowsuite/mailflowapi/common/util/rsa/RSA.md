### Creating a RSA key pair
```
openssl genrsa -out keypair.pem 2048
openssl rsa -in keypair.pem -pubout -out public.pem
openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in keypair.pem -out private.pem
```

### Base64 encoding RSA keys
```
base64 -i private.pem -o b64_private.txt
base64 -i public.pem -o b64_public.txt 
```

### Setting Base64 encodes RSA keys as .env variables
-  RSA_B64_PUBLIC_KEY 
- RSA_B64_PRIVATE_KEY