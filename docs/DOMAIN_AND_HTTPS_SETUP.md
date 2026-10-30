# SAGE — Domain & Automated HTTPS Setup Guide (Caddy)

> **Document Version**: 1.0.0  
> **Target Environment**: AWS EC2 with Caddy v2 Reverse Proxy  
> **Protocol**: Automated TLSv1.2 / TLSv1.3 via Let's Encrypt / ZeroSSL  

---

## 1. Architecture & HTTPS Workflow

In the SAGE production deployment on AWS EC2, **Caddy v2** serves as the public-facing edge reverse proxy. It automates:
- TLS certificate provisioning, verification, and automated renewal (90-day certificates renewed at 60 days).
- HTTP-to-HTTPS automatic redirection (port 80 to 443).
- HTTP/2 and HTTP/3 (QUIC) negotiation.
- Secure reverse proxying to internal Docker services over the bridge network.

```
       DNS A-Record: sage.aicte.gov.in -> AWS Elastic IP (e.g., 13.232.xxx.xxx)
                                      |
                                      v
+-----------------------------------------------------------------------------------+
|                        AWS EC2 INBOUND SECURITY GROUP                             |
|  - Port 22  (SSH)   -> Administrator IP only                                      |
|  - Port 80  (HTTP)  -> 0.0.0.0/0 (Required for ACME HTTP-01 challenge & redirect) |
|  - Port 443 (HTTPS) -> 0.0.0.0/0 (Public web traffic)                             |
+-----------------------------------------------------------------------------------+
                                      |
                                      v
+-----------------------------------------------------------------------------------+
|                              CADDY REVERSE PROXY                                  |
|  - Solves ACME HTTP-01 / TLS-ALPN-01 challenge with Let's Encrypt                 |
|  - Obtains wildcard or domain certificate and writes to `sage_caddy_data` volume  |
|  - Terminates TLS with modern cipher suites & HSTS                                |
|  - Proxies cleartext traffic internally to `http://frontend:80`                   |
+-----------------------------------------------------------------------------------+
```

---

## 2. Prerequisites & AWS Configuration

### 2.1 Allocate and Associate an Elastic IP
A static public IP is required so that DNS records remain stable across EC2 stop/start cycles:
1. Open the **Amazon EC2 Console** at `https://console.aws.amazon.com/ec2/`.
2. Under **Network & Security**, click **Elastic IPs**.
3. Click **Allocate Elastic IP address** (Region: `ap-south-1` Mumbai).
4. Select the allocated Elastic IP -> **Actions** -> **Associate Elastic IP address**.
5. Select your SAGE EC2 instance and private IP address -> Click **Associate**.

### 2.2 Configure Security Group Inbound Rules
Navigate to **EC2** -> **Security Groups** -> Select your instance's security group -> **Edit inbound rules**:

| Type | Protocol | Port Range | Source | Description |
| :--- | :--- | :--- | :--- | :--- |
| **SSH** | TCP | `22` | `Your-Office-or-Home-IP/32` | Secure administrative shell access |
| **HTTP** | TCP | `80` | `0.0.0.0/0` | ACME challenge & HTTP to HTTPS redirect |
| **HTTPS** | TCP | `443` | `0.0.0.0/0` | Secure public web access |

> [!IMPORTANT]
> Port 80 MUST be open to `0.0.0.0/0`. Let's Encrypt validates domain ownership by connecting back to port 80 (HTTP-01 challenge). If port 80 is blocked by your AWS security group, certificate issuance will fail.

---

## 3. DNS Configuration

In your DNS provider (AWS Route 53, Cloudflare, GoDaddy, Namecheap, etc.):
1. Create an **A Record**:
   - **Name / Host**: `sage` (or `@` for apex domain)
   - **Type**: `A`
   - **Value / Target**: `Your-AWS-Elastic-IP` (e.g. `13.232.xxx.xxx`)
   - **TTL**: `300` seconds (5 minutes for initial testing)
2. (Optional) If using Cloudflare: Set SSL/TLS encryption mode to **Full (strict)** and temporarily set proxy status to **DNS only** during initial certificate issuance.

---

## 4. Caddy Configuration Details

### The `infrastructure/Caddyfile`
```caddy
{
    email {$ACME_EMAIL:admin@example.com}
    admin off
    grace_period 10s
}

{$DOMAIN_NAME:localhost} {
    request_body {
        max_size 50MB
    }

    encode zstd gzip

    header {
        Strict-Transport-Security "max-age=31536000; includeSubDomains; preload"
        X-Content-Type-Options "nosniff"
        X-Frame-Options "SAMEORIGIN"
        Referrer-Policy "strict-origin-when-cross-origin"
        Permissions-Policy "camera=(), microphone=(), geolocation=()"
        -Server
    }

    reverse_proxy frontend:80 {
        header_up Host {host}
        header_up X-Real-IP {remote_host}
        header_up X-Forwarded-For {remote_host}
        header_up X-Forwarded-Proto {scheme}

        transport http {
            dial_timeout 10s
            response_header_timeout 180s
        }
    }

    log {
        output stdout
        format console
        level INFO
    }
}
```

---

## 5. Testing Without a Custom Domain (Staging & Evaluation)

If you do not yet have a custom domain registered, you can evaluate the production Compose setup in either of two ways:

### Option A: Using AWS Public IPv4 DNS
AWS EC2 provides an auto-generated public DNS hostname (e.g., `ec2-13-232-10-20.ap-south-1.compute.amazonaws.com`).
1. Set in `.env.production`:
   ```bash
   DOMAIN_NAME=ec2-13-232-10-20.ap-south-1.compute.amazonaws.com
   ACME_EMAIL=your-email@example.com
   ```
2. Caddy will successfully obtain a valid Let's Encrypt certificate for the AWS hostname!

### Option B: Raw IP / Localhost HTTP Mode
If testing strictly over HTTP without SSL:
1. Set in `.env.production`:
   ```bash
   DOMAIN_NAME=:80
   ```
2. Caddy will listen on port 80 and proxy traffic without attempting ACME certificate negotiation.

---

## 6. Certificate Verification & Troubleshooting

### Inspect Caddy Certificate Logs
```bash
docker compose -f infrastructure/docker-compose.prod.yml logs caddy
```
Look for:
```
certificate obtained successfully {"identifier": "sage.yourdomain.com", "issuer": "acmeserver"}
```

### Common Issues & Quick Fixes

1. **Error: `connection refused` on ACME challenge**:
   - Cause: AWS Security Group does not allow inbound port 80 from `0.0.0.0/0`.
   - Fix: Open port 80 in EC2 Security Group inbound rules.

2. **Error: `NXDOMAIN` or `No A records found`**:
   - Cause: DNS propagation delay.
   - Fix: Test with `dig +short sage.yourdomain.com` or `nslookup sage.yourdomain.com` on the EC2 host. Wait until it resolves to your Elastic IP.

3. **Certificate Volume Persistence**:
   - Caddy stores certificates in Docker volume `sage_caddy_data` mounted at `/data`.
   - Rebuilding or restarting the Caddy container will NEVER re-request certificates, avoiding Let's Encrypt rate limits (5 certificates per week per domain).
