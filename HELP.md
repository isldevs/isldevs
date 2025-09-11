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
  # -----------------
  # Build & Test (CI)
  # -----------------
  build:
    runs-on: ubuntu-latest
    permissions:
      contents: read

    outputs:
      jar-path: build/libs/*.jar

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
        with:
          gradle-version: 8.13

      - name: Clean Gradle cache
        run: ./gradlew clean --gradle-user-home=$GITHUB_WORKSPACE/.gradle

      - name: Build with Gradle Wrapper
        run: ./gradlew build -PskipIntegrationTests=true

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
        uses: gradle/actions/dependency-submission@v4.0.0

  # -----------
  # Deploy (CD)
  # -----------
  deploy:
    runs-on: ubuntu-latest
    needs: build
    # Only deploy on push to master/main, not on pull requests
    if: github.event_name == 'push' && (github.ref == 'refs/heads/master' || github.ref == 'refs/heads/main')

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Download JAR artifact
        uses: actions/download-artifact@v4
        with:
          name: isldevs-app
          path: app/

      - name: Setup SSH key
        run: |
          mkdir -p ~/.ssh
          echo "${{ secrets.SSH_PRIVATE_KEY }}" > ~/.ssh/deploy_key
          chmod 600 ~/.ssh/deploy_key

      - name: Test SSH connection
        run: |
          ssh -i ~/.ssh/deploy_key -o StrictHostKeyChecking=no ${{ secrets.SERVER_USER }}@${{ secrets.SERVER_HOST }} "echo SSH connection successful"

      - name: Deploy to EC2 via SSH
        uses: appleboy/ssh-action@v1.2.0
        with:
          host: ${{ secrets.SERVER_HOST }}
          username: ${{ secrets.SERVER_USER }}
          key: ${{ secrets.SSH_PRIVATE_KEY }}
          script: |
            # Create directory if it doesn't exist
            mkdir -p ~/isldevs-app
            
            # Stop existing application
            pkill -f 'java -jar isldevs.jar' || true
            
            # Copy new JAR file
            cp ~/app/*.jar ~/isldevs-app/isldevs.jar
            
            # Start application
            cd ~/isldevs-app
            nohup java -jar isldevs.jar --spring.config.location=classpath:/config/.prod > app.log 2>&1 &
            
            # Wait a moment and check if application started
            sleep 5
            ps aux | grep java
            
            echo "Deployment finished successfully."
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
ExecStart=/usr/bin/java -jar isldevs.jar --spring.config.location=classpath:/config/.prod
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
