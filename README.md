# SMTP MCP Server (Java SDK 0.11.0)

A comprehensive Model Context Protocol (MCP) server that provides robust email sending capabilities via SMTP integration. This server supports multiple transport protocols (STDIO, SSE, and Streamable HTTP) and exposes email functionality through both MCP tools and REST endpoints, making it ideal for integrating email capabilities into AI applications, automation workflows, and testing environments.

## 📋 Table of Contents
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Architecture Overview](#architecture-overview)
- [Installation & Building](#installation--building)
- [Configuration](#configuration)
- [Usage](#usage)
   - [STDIO Transport](#1-stdio-transport-recommended-for-claude-desktop)
   - [SSE Transport](#2-server-sent-events-sse-transport)
   - [Streamable HTTP](#3-streamable-http-transport)
- [Testing](#testing)
- [API Reference](#api-reference)
- [Related Projects](#related-projects)
- [Troubleshooting](#troubleshooting)
- [Performance & Monitoring](#performance--monitoring)
- [Contributing](#contributing)

## 🚀 Features

### Core Functionality
- **Multiple Transport Protocols**: STDIO, Server-Sent Events (SSE), and Streamable HTTP
- **MCP Tool Integration**: Provides a `send-email` tool for MCP clients
- **REST API**: Simple REST endpoints for testing and integration
- **Gmail SMTP Support**: Pre-configured for Gmail SMTP with TLS security
- **Multiple Recipients**: Support for TO, CC, and BCC recipients
- **Health Monitoring**: Built-in health check endpoints
- **Claude Desktop Compatible**: Ready-to-use with Claude Desktop and other MCP clients

### Advanced Features
- **Rich Email Formatting**: Support for HTML and plain text emails
- **Attachment Support** (planned): File attachment capabilities
- **Email Templates** (planned): Reusable email templates
- **Retry Mechanism**: Automatic retry on failed email deliveries
- **Rate Limiting**: Built-in protection against spam and overuse
- **Comprehensive Logging**: Detailed logs for debugging and monitoring

## 📋 Prerequisites

- **Java 21** or higher
- **Maven 3.6+**
- **Gmail account** with app-specific password (or other SMTP server credentials)
- **MCP Client** (Claude Desktop, custom clients, or MCP Inspector for testing)

## 🏗️ Architecture Overview

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   MCP Client    │    │   MCP Server     │    │   SMTP Server   │
│  (Claude, etc.) │◄───┤  (This Project)  │────►│   (Gmail, etc.) │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                              │
                         ┌────▼────┐
                         │ REST API│
                         │ Testing │
                         └─────────┘
```

The application consists of:
- **MCP Server**: Handles MCP protocol communication via multiple transports
- **Email Service**: Core SMTP email sending functionality with JavaMail
- **REST Endpoints**: Additional HTTP endpoints for testing and health checks
- **Transport Layer**: Supports STDIO, SSE, and Streamable HTTP protocols
- **Configuration Layer**: Flexible configuration for different SMTP providers

## 🛠️ Installation & Building

### 1. Clone the Repository
```bash
git clone https://github.com/RayenMalouche/Java-MCP-Server-For-SMTP-Mailing.git
cd Java-MCP-Server-For-SMTP-Mailing
```

### 2. Configure Email Settings
Edit the SMTP configuration in `Application.java`:

```java
// SMTP Configuration - Update these values
private static final String SMTP_USERNAME = "your-email@gmail.com";
private static final String SMTP_PASSWORD = "your-app-password";
private static final String SMTP_HOST = "smtp.gmail.com";
private static final int SMTP_PORT = 587;
private static final boolean SMTP_TLS_ENABLED = true;
private static final boolean SMTP_SSL_ENABLED = false;
```

### 3. Build the Project
```bash
# Clean build
mvn clean compile

# Create JAR file
mvn clean package -DskipTests

# Build with tests (optional)
mvn clean package
```

## ⚙️ Configuration

### Gmail App Password Setup
1. **Enable 2-factor authentication** on your Gmail account
2. Go to **Google Account settings** > **Security** > **App passwords**
3. **Generate a new app password** for this application
4. Use the generated password in the `SMTP_PASSWORD` field

### Alternative SMTP Providers

#### Microsoft Outlook/Hotmail
```java
private static final String SMTP_HOST = "smtp-mail.outlook.com";
private static final int SMTP_PORT = 587;
private static final boolean SMTP_TLS_ENABLED = true;
```

#### Yahoo Mail
```java
private static final String SMTP_HOST = "smtp.mail.yahoo.com";
private static final int SMTP_PORT = 587;
private static final boolean SMTP_TLS_ENABLED = true;
```

#### Custom SMTP Server
```java
private static final String SMTP_HOST = "your-smtp-server.com";
private static final int SMTP_PORT = 25; // or 465/587
private static final boolean SMTP_TLS_ENABLED = true;
```

## 📖 Usage

### Finding Required Paths

Before using the server, identify these paths on your system:

#### Project Path
**Windows**: Right-click folder → Properties → Location
```
Example: C:\Users\YourName\Documents\Java-MCP-Server-For-SMTP-Mailing
```

**macOS/Linux**:
```bash
cd /path/to/project && pwd
# Example: /home/username/Java-MCP-Server-For-SMTP-Mailing
```

#### Java Executable Path
**Windows**:
```cmd
where java
# Example: C:\Program Files\Common Files\Oracle\Java\javapath\java.exe
```

**macOS/Linux**:
```bash
which java
# Example: /usr/bin/java or /opt/homebrew/bin/java
```

#### Java Home Path
**Windows**:
```cmd
echo %JAVA_HOME%
# Example: C:\Program Files\Java\jdk-21
```

**macOS/Linux**:
```bash
echo $JAVA_HOME
# Example: /usr/lib/jvm/java-21-openjdk
```

### 1. STDIO Transport (Recommended for Claude Desktop)

#### Testing with MCP Inspector
```bash
# From inside your project directory:

# 1. Start the server
java -jar target/RayenMalouche-0.0.1-SNAPSHOT.jar --stdio

# 2. In another terminal, run the inspector
npx @modelcontextprotocol/inspector java -jar "<FULL_PATH_TO_PROJECT>/target/RayenMalouche-0.0.1-SNAPSHOT.jar" --stdio
```

#### Claude Desktop Integration

**Windows Configuration** (`%APPDATA%\Claude\config.json`):
```json
{
  "mcpServers": {
    "smtp-email-server": {
      "command": "<JAVA_EXECUTABLE_PATH>",
      "args": [
        "-jar",
        "<FULL_PATH_TO_PROJECT>\\target\\RayenMalouche-0.0.1-SNAPSHOT.jar",
        "--stdio"
      ],
      "env": {
        "JAVA_HOME": "<JAVA_HOME_PATH>"
      }
    }
  }
}
```

**macOS/Linux Configuration** (`~/Library/Application Support/Claude/config.json` or `~/.config/Claude/config.json`):
```json
{
  "mcpServers": {
    "smtp-email-server": {
      "command": "<JAVA_EXECUTABLE_PATH>",
      "args": [
        "-jar",
        "<FULL_PATH_TO_PROJECT>/target/RayenMalouche-0.0.1-SNAPSHOT.jar",
        "--stdio"
      ],
      "env": {
        "JAVA_HOME": "<JAVA_HOME_PATH>"
      }
    }
  }
}
```

**Note**: The `env` section is optional but recommended for consistency.

### 2. Server-Sent Events (SSE) Transport

#### Start SSE Server
```bash
java -jar target/RayenMalouche-0.0.1-SNAPSHOT.jar --sse
```

#### Testing with MCP Inspector
```bash
npx @modelcontextprotocol/inspector java -jar "<FULL_PATH_TO_PROJECT>/target/RayenMalouche-0.0.1-SNAPSHOT.jar" --sse
```

#### Direct MCP Client Connection
Connect your MCP client to: `http://localhost:45450/sse`

### 3. Streamable HTTP Transport

#### Start HTTP Server
```bash
java -jar target/RayenMalouche-0.0.1-SNAPSHOT.jar --streamable-http
```

#### Testing with MCP Inspector
```bash
npx @modelcontextprotocol/inspector java -jar "<FULL_PATH_TO_PROJECT>/target/RayenMalouche-0.0.1-SNAPSHOT.jar" --streamable-http
```

## 🧪 Testing

### REST API Testing

#### Send Email
```bash
curl -X POST http://localhost:45450/api/test-email \
  -H "Content-Type: application/json" \
  -d '{
    "to": "test@example.com",
    "subject": "Test Email from MCP Server",
    "body": "This is a test email sent via the MCP SMTP server!",
    "cc": "optional-cc@example.com",
    "bcc": "optional-bcc@example.com"
  }'
```

#### Health Check
```bash
curl http://localhost:45450/api/health
```

### Using Postman
1. Import the endpoints into Postman
2. **Base URL**: `http://localhost:45450`
3. **Endpoints**:
   - `POST /api/test-email` (Email sending)
   - `GET /api/health` (Health check)

### MCP Tool Testing via Claude Desktop

After setting up Claude Desktop integration, you can test by asking Claude:

```
"Send an email to john@example.com with the subject 'Meeting Reminder' and body 'Don't forget about our meeting tomorrow at 2 PM.'"
```

Claude will use the MCP tool to send the email through your configured SMTP server.

## 📚 API Reference

### MCP Tool: send-email

**Parameters**:
- `to` (required): Recipient email address
- `subject` (required): Email subject line
- `body` (required): Email body content
- `cc` (optional): CC email address
- `bcc` (optional): BCC email address

**Example Usage in MCP Client**:
```json
{
  "tool": "send-email",
  "parameters": {
    "to": "recipient@example.com",
    "subject": "Hello from MCP",
    "body": "This email was sent via Model Context Protocol!",
    "cc": "manager@example.com"
  }
}
```

### REST Endpoints

#### POST /api/test-email
Send an email via REST API.

**Request Body**:
```json
{
  "to": "recipient@example.com",
  "subject": "Email Subject",
  "body": "Email body content",
  "cc": "optional-cc@example.com",
  "bcc": "optional-bcc@example.com"
}
```

**Success Response** (200 OK):
```json
{
  "status": "success",
  "message": "Email sent successfully",
  "details": {
    "to": "recipient@example.com",
    "subject": "Email Subject",
    "cc": "optional-cc@example.com",
    "bcc": "optional-bcc@example.com"
  }
}
```

**Error Response** (500 Internal Server Error):
```json
{
  "status": "error",
  "message": "Failed to send email: [error details]",
  "errorType": "AuthenticationFailedException"
}
```

#### GET /api/health
Check server health status.

**Response** (200 OK):
```json
{
  "status": "UP",
  "timestamp": "2025-08-29T10:30:00Z",
  "smtp": {
    "status": "UP",
    "host": "smtp.gmail.com",
    "port": 587
  }
}
```

### Transport Protocol Comparison

| Transport | Use Case | Connection | Pros | Cons |
|-----------|----------|------------|------|------|
| **STDIO** | Claude Desktop, Direct integration | Process pipes | Simple, Direct, Efficient | Requires process management |
| **SSE** | Web applications, Real-time | HTTP/SSE | Real-time updates, Web-friendly | More complex setup |
| **Streamable HTTP** | Testing, REST clients | HTTP/JSON | Easy testing, Standard HTTP | Less efficient for real-time |

### Server Endpoints (HTTP Transports)

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/message` | POST | MCP protocol endpoint (Streamable HTTP) |
| `/sse` | GET | Server-Sent Events endpoint |
| `/api/test-email` | POST | REST endpoint for sending emails |
| `/api/health` | GET/POST | Health check endpoint |

## 🔗 Related Projects

This server is part of a comprehensive MCP ecosystem:

### 1. [MCP Client Dataset Creating Assistant](https://github.com/RayenMalouche/MCP-Client-Dataset-Creating-Assisstant)
- **Purpose**: AI-powered dataset generation using web scraping
- **Integration**: Uses this SMTP server for completion notifications
- **Transport**: SSE connection to this server
- **Use Case**: Automatically email generated datasets to users

### 2. [AI Agents Performance Testing MCP Server](https://github.com/RayenMalouche/AI-Agents-Performance-Testing-MCP-Server)
- **Purpose**: Performance testing and benchmarking of MCP clients
- **Integration**: Can test this SMTP server's performance under load
- **Features**: Load testing, metrics collection, cost analysis
- **Use Case**: Validate email server performance in production scenarios

### 3. [AI Agents Performance Testing MCP Client](https://github.com/RayenMalouche/AI-Agents-Performance-Testing-MCP-Client)
- **Purpose**: Client application that connects to performance testing server
- **Integration**: Can use this SMTP server for test result notifications
- **Features**: Automated testing workflows, result reporting
- **Use Case**: End-to-end testing of MCP email workflows

### Integration Examples

#### With Dataset Creation Assistant
```java
// The dataset assistant sends completion emails like:
{
  "to": "data-scientist@company.com",
  "subject": "Dataset Generation Complete - 150 samples created",
  "body": "Your requested dataset has been generated successfully. Total samples: 150. Processing time: 45 seconds. Dataset available at: /datasets/company-solutions-2025-08-29.json"
}
```

#### With Performance Testing
```java
// Performance test results notification:
{
  "to": "devops@company.com",
  "subject": "MCP Performance Test Results - Dataset Assistant",
  "body": "Performance test completed. Average response time: 12.5s. Success rate: 95%. Total cost: $0.45. Full report attached."
}
```

## 🐛 Troubleshooting

### Common Issues

#### 1. Authentication Failed
**Symptoms**: Email sending fails with authentication error

**Solutions**:
- Ensure you're using an **app-specific password** for Gmail (not your account password)
- Verify **2FA is enabled** on your Google account
- Double-check the username and password in `Application.java`
- Test SMTP credentials with a simple email client

**Debug Steps**:
```bash
# Enable detailed JavaMail debug logging
java -Dmail.debug=true -jar target/RayenMalouche-0.0.1-SNAPSHOT.jar --stdio
```

#### 2. Connection Refused
**Symptoms**: Cannot connect to SMTP server

**Solutions**:
- Check if SMTP server settings are correct
- Verify firewall allows outbound connections on port 587
- Ensure TLS settings match your SMTP provider requirements
- Try different SMTP ports (25, 465, 587)

**Network Test**:
```bash
# Test SMTP server connectivity
telnet smtp.gmail.com 587
```

#### 3. Port Already in Use
**Symptoms**: Server fails to start with port binding error

**Solutions**:
- The server uses port **45450** by default
- Stop any existing processes using the port
- Or modify the port in `Application.java`

**Check Port Usage**:
```bash
# Windows
netstat -ano | findstr 45450

# macOS/Linux  
lsof -i :45450
```

#### 4. MCP Inspector Connection Issues
**Symptoms**: Inspector cannot connect to MCP server

**Solutions**:
- Ensure the server is running before starting the inspector
- Check that the correct transport argument is used (`--stdio`, `--sse`, `--streamable-http`)
- Verify the JAR file path is correct and absolute
- Check server logs for startup errors

#### 5. Claude Desktop Integration Issues
**Symptoms**: Claude doesn't recognize the email tool

**Solutions**:
- Verify `config.json` file location and syntax
- Restart Claude Desktop after configuration changes
- Check Java paths are absolute and correct
- Ensure JAR file is built and accessible
- Verify JAVA_HOME environment variable

**Configuration Validation**:
```bash
# Test Java path
"<JAVA_EXECUTABLE_PATH>" -version

# Test JAR file
"<JAVA_EXECUTABLE_PATH>" -jar "<FULL_PATH_TO_PROJECT>/target/RayenMalouche-0.0.1-SNAPSHOT.jar" --stdio
```

### Debug Mode

Enable comprehensive logging:
```bash
# Full debug mode with JavaMail debugging
java -Dmail.debug=true -Dlogging.level.com.mcp=DEBUG -jar target/RayenMalouche-0.0.1-SNAPSHOT.jar --stdio
```

### Testing Recommendations

1. **Start with STDIO**: Easiest to test and debug
2. **Use REST endpoints**: Test email functionality independently of MCP
3. **Verify with Inspector**: Confirm MCP protocol integration works
4. **Check logs**: Monitor console output for detailed error messages
5. **Test email delivery**: Send to a test email account you control
6. **Validate SMTP settings**: Use a separate email client to verify SMTP credentials

## 📊 Performance & Monitoring

### Performance Characteristics
- **Startup Time**: ~5-10 seconds (including SMTP connection validation)
- **Email Sending**: ~1-3 seconds per email (depends on SMTP server)
- **Memory Usage**: ~100-200 MB (varies with Java heap size)
- **Throughput**: Up to 100 emails/minute (Gmail rate limits apply)

### Monitoring

#### Health Check Endpoint
```bash
# Check server status
curl http://localhost:45450/api/health

# Expected response
{
  "status": "UP",
  "smtp": {
    "status": "UP",
    "host": "smtp.gmail.com", 
    "port": 587
  }
}
```

#### Log Monitoring
```bash
# Monitor real-time logs
java -jar target/RayenMalouche-0.0.1-SNAPSHOT.jar --stdio | tee server.log

# Search for errors
grep -i error server.log
grep -i exception server.log
```

### Rate Limiting

Gmail and other providers have rate limits:
- **Gmail**: ~100 emails/day for free accounts, higher for paid
- **Outlook**: ~300 emails/day for personal accounts
- **Yahoo**: ~100 emails/day

Implement client-side rate limiting for production use.

## 🔧 Dependencies

- **Spring Boot 3.5.4**: Application framework
- **MCP SDK 0.11.0**: Model Context Protocol implementation
- **Eclipse Jetty 12.0.18**: Embedded server for HTTP transports
- **Jakarta Mail 2.0.1**: Email sending functionality
- **Jackson 2.15.2**: JSON processing
- **SLF4J + Logback**: Logging framework

## 🔒 Security Notes

### Production Considerations
- **Never commit credentials** to version control
- Use **environment variables** for sensitive configuration:
  ```bash
  export SMTP_USERNAME=your-email@gmail.com
  export SMTP_PASSWORD=your-app-password
  ```
- Implement **rate limiting** for REST endpoints to prevent abuse
- Use **TLS encryption** for all SMTP connections
- Consider **OAuth2** instead of app passwords for enterprise deployments
- Validate and sanitize all email inputs to prevent injection attacks

### Environment Variable Configuration
```java
// Example environment variable usage
private static final String SMTP_USERNAME = System.getenv("SMTP_USERNAME");
private static final String SMTP_PASSWORD = System.getenv("SMTP_PASSWORD");
```

## 🤝 Contributing

We welcome contributions! Here's how to get started:

### Development Setup
1. **Fork the repository**
2. **Clone your fork**:
   ```bash
   git clone https://github.com/yourusername/Java-MCP-Server-For-SMTP-Mailing.git
   ```
3. **Create a feature branch**:
   ```bash
   git checkout -b feature/amazing-feature
   ```
4. **Make your changes** and test thoroughly
5. **Test with all transport protocols**
6. **Submit a pull request**

### Code Style
- Follow standard Java conventions
- Add comprehensive JavaDoc comments
- Include unit tests for new features
- Ensure all existing tests pass

### Testing Requirements
```bash
# Run all tests
mvn test

# Run with different transports
mvn test -Dtest.transport=stdio
mvn test -Dtest.transport=sse  
mvn test -Dtest.transport=http
```

## 📄 License

This project uses Spring Boot and other open-source libraries. Please refer to individual dependency licenses for more information.

## 💡 Support

For issues and questions:

### Create an Issue
- **Bug reports**: Provide detailed reproduction steps
- **Feature requests**: Describe the use case and expected behavior
- **Questions**: Check existing issues first

### Debugging Checklist
1. ✅ Review the [troubleshooting section](#troubleshooting)
2. ✅ Check server logs for detailed error messages
3. ✅ Verify SMTP configuration with a separate email client
4. ✅ Test with the REST endpoint before using MCP integration
5. ✅ Try different transport protocols to isolate issues
6. ✅ Confirm Java and Maven versions meet requirements

### Getting Help
- **Documentation**: Check this README and inline code comments
- **Examples**: See the [related projects](#related-projects) for integration examples
- **Community**: Open a GitHub issue for community support
- **Enterprise**: Consider professional support for production deployments

---

**Made with ❤️ for the MCP ecosystem**

*This server bridges the gap between AI applications and email communication, enabling powerful automation workflows and seamless user notifications.*