# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/3.4.3/gradle-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/3.4.3/gradle-plugin/packaging-oci-image.html)
* [GraalVM Native Image Support](https://docs.spring.io/spring-boot/3.4.3/reference/packaging/native-image/introducing-graalvm-native-images.html)
* [Spring Web](https://docs.spring.io/spring-boot/3.4.3/reference/web/servlet.html)
* [Spring Boot DevTools](https://docs.spring.io/spring-boot/3.4.3/reference/using/devtools.html)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)

### Additional Links
These additional references should also help you:

* [Gradle Build Scans – insights for your project's build](https://scans.gradle.com#gradle)
* [Configure AOT settings in Build Plugin](https://docs.spring.io/spring-boot/3.4.3/how-to/aot.html)

## GraalVM Native Support

This project has been configured to let you generate either a lightweight container or a native executable.
It is also possible to run your tests in a native image.

### Lightweight Container with Cloud Native Buildpacks
If you're already familiar with Spring Boot container images support, this is the easiest way to get started.
Docker should be installed and configured on your machine prior to creating the image.

To create the image, run the following goal:

```
$ ./gradlew bootBuildImage
```

Then, you can run the app like any other container:

```
$ docker run --rm -p 8080:8080 isldevs:0.0.1-SNAPSHOT
```

### Executable with Native Build Tools
Use this option if you want to explore more options such as running your tests in a native image.
The GraalVM `native-image` compiler should be installed and configured on your machine.

NOTE: GraalVM 22.3+ is required.

To create the executable, run the following goal:

```
$ ./gradlew nativeCompile
```

Then, you can run the app as follows:
```
$ build/native/nativeCompile/isldevs
```

You can also run your existing tests suite in a native image.
This is an efficient way to validate the compatibility of your application.

To run your existing tests in a native image, run the following goal:

```
$ ./gradlew nativeTest
```

### Gradle Toolchain support

There are some limitations regarding Native Build Tools and Gradle toolchains.
Native Build Tools disable toolchain support by default.
Effectively, native image compilation is done with the JDK used to execute Gradle.
You can read more about [toolchain support in the Native Build Tools here](https://graalvm.github.io/native-build-tools/latest/gradle-plugin.html#configuration-toolchains).

# GitHub Actions CI/CD Pipeline for Spring Boot to EC2

## 📋 Prerequisites

### 1. AWS EC2 Instance Requirements
- **Instance**: t2.micro or higher
- **OS**: Amazon Linux 2 or Ubuntu
- **Security Group**: Open ports 22 (SSH), 80 (HTTP), 443 (HTTPS)
- **Java**: JDK 21 installed
- **Git**: Installed on EC2

### 2. GitHub Repository Requirements
- Spring Boot application with Gradle
- Application code in repository
- GitHub Actions enabled

## 🚀 Step 1: EC2 Instance Setup

### 1.1 Launch EC2 Instance
```bash
# Connect to your EC2 instance
ssh -i your-key.pem ec2-user@your-ec2-ip
```

### 1.2 Install Java 21
- For Amazon Linux 2
```
sudo amazon-linux-extras enable java-openjdk21
sudo yum install -y java-21-openjdk-devel
```

- For Ubuntu
```
sudo apt update
sudo apt install -y openjdk-21-jdk
```

- Verify installation
```
java -version
```

### 1.3 Create Application Directory
```
mkdir ~/isldevs-app
cd ~/isldevs-app
```

### 1.4 Generate SSH Key for GitHub Actions
```
# Generate new ED25519 key pair
ssh-keygen -t ed25519 -f ~/github-ci-key -N "" -C "github-ci"

# Add public key to authorized_keys
cat ~/github-ci-key.pub >> ~/.ssh/authorized_keys

# Set proper permissions
chmod 700 ~/.ssh
chmod 600 ~/.ssh/authorized_keys

# Get the private key for GitHub
cat ~/github-ci-key
```

## 🛠️ Step 2: GitHub Repository Setup

### 2.1 Configure GitHub Secrets
Go to your repository → Settings → Secrets and variables → Actions
Add these secrets:

| Secret Name       | Value                                    |
|-------------------|------------------------------------------|
| `SERVER_HOST`     | Your EC2 public IP                       |
| `SERVER_USER`     | ec2-user                                 |
| `SSH_PRIVATE_KEY` | The private key from cat ~/github-ci-key |

### 2.2 Create GitHub Actions Workflow
Create file: .github/workflows/ci-cd-pipeline.yml
```
name: ISLDevs CI/CD Pipeline

on:
  push:
    branches: [ "master", "main" ]
  pull_request:
    branches: [ "master", "main" ]

jobs:
  build:
    runs-on: ubuntu-latest
    permissions:
      contents: read

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: gradle

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4.0.0

      - name: Build with Gradle
        run: ./gradlew build -x test

      - name: Run Tests
        run: ./gradlew test

      - name: Upload JAR artifact
        uses: actions/upload-artifact@v4
        with:
          name: isldevs-app
          path: build/libs/*.jar

  dependency-submission:
    runs-on: ubuntu-latest
    permissions:
      contents: write
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
      - name: Generate and submit dependency graph
        uses: gradle/actions/dependency-submission@af1da67850ed9a4cedd57bfd976089dd991e2582 # v4.0.0
        
  deploy:
    runs-on: ubuntu-latest
    needs: build

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Download JAR artifact
        uses: actions/download-artifact@v4
        with:
          name: isldevs-app
          path: ./artifact
      - name: Setup key
        id: setup-key
        env:
          SSH_PRIVATE_KEY: ${{ secrets.SSH_PRIVATE_KEY }}
        run: |
          echo "$SSH_PRIVATE_KEY" >> $HOME/isldevs.pem
          chmod 400 $HOME/isldevs.pem

      - name: Copy JAR to EC2
        uses: appleboy/scp-action@master
        with:
          host: ${{ secrets.SERVER_HOST }}
          username: ${{ secrets.SERVER_USER }}
          key: ${{ secrets.SSH_PRIVATE_KEY }}
          source: "./artifact/*.jar"
          target: "/home/ec2-user/isldevs-app/"

      - name: Connect SSH and Restart Services
        env:
          DB_HOST: ${{ secrets.DB_HOST }}
          DB_PASSWORD: ${{ secrets.DB_PASSWORD }}
        run: |
          ssh -o StrictHostKeyChecking=no -i $HOME/isldevs.pem ec2-user@${{ secrets.SERVER_HOST }} "
            export DB_HOST=$DB_HOST
            export DB_PASSWORD=$DB_PASSWORD
            sudo systemctl restart isldevs
          "
```

## 📁 Step 3: EC2 Post-Deployment Setup
### 3.1 Create Startup Script on EC2
Create ~/isldevs-app/start.sh:
```
#!/bin/bash

# Stop existing application
pkill -f 'java -jar isldevs.jar' || true

# Start application
cd ~/isldevs-app
nohup java -jar isldevs.jar --spring.config.location=classpath:/config/.prod > app.log 2>&1 &

echo "Application started"
```
Make it executable:
>chmod +x ~/isldevs-app/start.sh

### 3.2 Create Systemd Service (Optional)
Create /etc/systemd/system/isldevs.service:
```
[Unit]
Description=ISLDevs Spring Boot Application
After=network.target

[Service]
User=ec2-user
WorkingDirectory=/home/ec2-user/isldevs-app
ExecStart=/usr/bin/java -jar /home/ec2-user/isldevs-app/artifact/isldevs.jar --spring.profiles.active=prod
Environment="SPRING_DATASOURCE_URL=jdbc:postgresql://your-db-host:5432/your-db-name"
Environment="SPRING_DATASOURCE_USERNAME=your-db-user"
Environment="SPRING_DATASOURCE_PASSWORD=your-db-password"
SuccessExitStatus=143
Restart=always
RestartSec=30

[Install]
WantedBy=multi-user.target
```
Enable and start service:
```
sudo systemctl daemon-reload
sudo systemctl enable isldevs.service
sudo systemctl start isldevs.service
```

### 3.3 Test the CD Pipeline
```
# Check if application is running
ps aux | grep java

# Check application logs
tail -f ~/isldevs-app/app.log

# Check application logs (EC2)
sudo journalctl -u isldevs.service -f

# Test application endpoint
curl http://localhost:8080/health
```

### 3.4 Troubleshooting Common Issues
SSH Connection Issues
```
# Test SSH connection manually
ssh -i private-key.pem ec2-user@your-ec2-ip

# Check SSH logs on EC2
sudo tail -f /var/log/secure
```
Permission Issues
```
# Fix directory permissions on EC2
chown -R ec2-user:ec2-user ~/isldevs-app
chmod 755 ~/isldevs-app
```
Application Not Starting
```
# Check Java version
java -version

# Check available memory
free -h

# Check application logs
tail -f ~/isldevs-app/app.log
```
Port Already in Use
```
# Find process using port 8080
sudo netstat -tulpn | grep :8080

# Kill process if needed
sudo kill -9 <PID>
```

## Migration data from Source(MySQL) to Target(PostgreSQL)

### Install pgloader package
1. Linux/Mac
    * sudo apt install pgloader

### Prepare the pgloader Config File

1. **URL-encode MySQL password**
    - Original password: `Pass@2021!`
    - URL-encoded: `Pass%402021%21`
        - `@` → `%40`
        - `!` → `%21`

2. **Create a pgloader config file** named `mysql_to_pg.load` with the following content:

```lisp
LOAD DATABASE
     FROM mysql://DB_USERNAME:Pass%402021%21@{IP}:{PORT}/{DATABASE}
     INTO postgresql://postgres:password@localhost/isldevs_db

INCLUDING ONLY TABLE NAMES MATCHING 'table_A'
INCLUDING ONLY TABLE NAMES MATCHING 'table_B'

WITH include no drop, create tables, create indexes, reset sequences

SET work_mem to '16MB', maintenance_work_mem to '512 MB'

-- Convert MySQL datetime → Postgres timestamptz
CAST type datetime to timestamptz drop default using zero-dates-to-null

WITH SCHEMA MAPPING
     {DATABASE} TO 'public';
```

3. **Test Postgres Connection**
   >mysql -h {IP} -u {DB_USERNAME} -p

   >psql -h {IP} -u {DB_USERNAME} -d isldevs_db

4. **Run migration**

```bash
   pgloader mysql_to_pg.load
 ```

## Redis Setup Guide

### Ubuntu

```bash
sudo apt update
sudo apt install redis-server -y
sudo systemctl enable redis
sudo systemctl start redis
sudo systemctl status redis

```

* Test Connection

```bash
redis-cli
ping
# PONG
```

* Set Password

```bash
sudo nano /etc/redis/redis.conf
# Find and set:
# requirepass yourpassword
sudo systemctl restart redis
```

* Authenticate

```bash
redis-cli -a yourpassword
```

### Windows

Redis doesn’t have an official Windows build.
Use Docker or WSL2.

* Option A: Docker

```bash
docker run --name redis-local -p 6379:6379 -d redis:7-alpine
docker volume create redis-data
docker run --name redis-local \
  -p 6379:6379 \
  -v redis-data:/data \
  -d redis:7-alpine \
  redis-server --appendonly yes
```

* Option B: WSL (Ubuntu)

1. Install WSL2
2. Open Ubuntu terminal
3. Follow the Ubuntu steps above.
4. Test Redis

```bash
redis-cli -h 127.0.0.1 -p 6379 -a yourpassword
ping
# PONG
```

## SSH Key Setup Instructions

### 1. Generate SSH Keys
Generate separate SSH keys for GitHub and GitLab using the `ed25519` algorithm.

```bash
ssh-keygen -t ed25519 -C "username@example.com" -f ~/.ssh/id_ed25519_github
ssh-keygen -t ed25519 -C "username@example.com" -f ~/.ssh/id_ed25519_gitlab
```

- **Options**:
   - `-t ed25519`: Specifies the key type (Ed25519 for modern security).
   - `-C "username@example.com"`: Adds a comment with your email for identification.
   - `-f ~/.ssh/id_ed25519_github`: Specifies the file path for the GitHub key.
   - `-f ~/.ssh/id_ed25519_gitlab`: Specifies the file path for the GitLab key.
- When prompted, press Enter to use no passphrase, or set one for added security.

### 2. Add SSH Keys to SSH Agent
Start the SSH agent and add the private keys to it.

```bash
eval "$(ssh-agent -s)"
ssh-add ~/.ssh/id_ed25519_github
ssh-add ~/.ssh/id_ed25519_gitlab
```

- **Explanation**:
   - `eval "$(ssh-agent -s)"`: Starts the SSH agent in the background.
   - `ssh-add`: Adds the private keys to the agent for authentication.

### 3. Copy Public Keys to Clipboard
Copy the public keys to your clipboard to add them to GitHub and GitLab.

**Using `xclip` (Linux)**:
```bash
cat ~/.ssh/id_ed25519_github.pub | xclip -selection clipboard
cat ~/.ssh/id_ed25519_gitlab.pub | xclip -selection clipboard
```

**Alternative (manual display)**:
```bash
cat ~/.ssh/id_ed25519_github.pub
cat ~/.ssh/id_ed25519_gitlab.pub
```

- Copy the output and add it to:
   - **GitHub**: Settings > SSH and GPG keys > New SSH key.
   - **GitLab**: Settings > SSH Keys > Add SSH Key.

### 4. Configure SSH for GitHub and GitLab
Create or edit the SSH configuration file to specify which key to use for each service.

```bash
nano ~/.ssh/config
```

Add the following content to `~/.ssh/config`:

```
# GitHub
Host github.com
  HostName github.com
  User git
  IdentityFile ~/.ssh/id_ed25519_github

# GitLab
Host gitlab.com
  HostName gitlab.com
  User git
  IdentityFile ~/.ssh/id_ed25519_gitlab
```

Set the correct permissions for the config file:

```bash
chmod 600 ~/.ssh/config
```

- **Explanation**:
   - `Host`: Defines a shorthand name for the service.
   - `HostName`: Specifies the actual hostname.
   - `User git`: Uses the `git` user for SSH connections.
   - `IdentityFile`: Points to the specific private key for each service.
   - `chmod 600`: Ensures the config file is only readable/writable by the owner.

### 5. Test SSH Connections
Verify that the SSH keys work for GitHub and GitLab.

```bash
ssh -T git@github.com
ssh -T git@gitlab.com
```

- **Expected Output**:
   - GitHub: `Hi username! You've successfully authenticated...`
   - GitLab: `Welcome to GitLab, @username!`

### 6. Configure Git Remotes
Set up the repository to push to both GitHub and GitLab.

Check current remotes:
```bash
git remote -v
```

Add GitHub and GitLab as push URLs for the `origin` remote:
```bash
git remote set-url --add --push origin git@github.com:username/projectname.git
git remote set-url --add --push origin git@gitlab.com:username/projectname.git
```

- **Explanation**:
   - `git remote set-url --add --push`: Adds multiple push URLs to the `origin` remote.
   - This allows pushing to both GitHub and GitLab with a single `git push`.

### 7. Push to Both Repositories
Push changes to both GitHub and GitLab.

```bash
git push origin master
```

Alternatively, push to GitLab explicitly:
```bash
git push gitlab master
```

- **Note**: Ensure the `master` branch exists. If your default branch is `main`, replace `main` with `master`.

## Troubleshooting
- **SSH Connection Issues**:
   - Verify key permissions: `ls -l ~/.ssh/` (should be `600` for private keys, `644` for public keys).
   - Check SSH agent: `ssh-add -l` to list loaded keys.
   - Debug SSH: `ssh -vT git@github.com` or `ssh -vT git@gitlab.com`.

- **Clipboard Issues**:
   - If `xclip` is unavailable, manually copy the output of `cat ~/.ssh/id_ed25519_*.pub`.
   - On macOS, use `pbcopy`: `cat ~/.ssh/id_ed25519_github.pub | pbcopy`.

- **Git Push Errors**:
   - Ensure the repository exists on both GitHub (`git@github.com:username/projectname.git`) and GitLab (`git@gitlab.com:username/projectname.git`).
   - Verify branch name: `git branch` to confirm `main` or `master`.

## Notes
- Replace `yourgmail@gmail.com` with your actual email if different.
- Ensure `username/projectname` repositories exist on both GitHub and GitLab.
- For added security, consider using a passphrase for SSH keys and manage with `ssh-agent`.