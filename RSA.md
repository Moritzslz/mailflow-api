### Creating a RSA key pair
```
openssl genrsa -out keypair.pem 2048
openssl rsa -in keypair.pem -outform PEM -pubout -out public.pem
openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in keypair.pem -out private.pem
```

### Setting RSA keys as .env variables
- RSA_PUBLIC_KEY 
- RSA_PRIVATE_KEY