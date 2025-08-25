# SMTP MCP Server (JAVA sdk 0.11.0 version)

A Model Context Protocol (MCP) server that provides email sending capabilities via SMTP. This server supports multiple transport protocols (STDIO, SSE, and Streamable HTTP) and exposes email functionality through both MCP tools and REST endpoints, making it easy to integrate email sending into AI applications and other systems.

## Features

- **Multiple Transport Protocols**: STDIO, Server-Sent Events (SSE), and Streamable HTTP
- **MCP Tool Integration**: Provides a `send-email` tool for MCP clients
- **REST API**: Simple REST endpoints for testing and integration
- **Gmail SMTP Support**: Pre-configured for Gmail SMTP with TLS
- **Multiple Recipients**: Support for TO, CC, and BCC recipients
- **Health Monitoring**: Built-in health check endpoint
- **Claude Desktop Compatible**: Ready for integration with Claude Desktop

## Prerequisites

- Java 21 or higher
- Maven 3.6+
- Gmail account with app-specific password (or other SMTP server credentials)

## Configuration

### SMTP Settings

Update the SMTP credentials directly in the `Application.java` file:

```java
private static final String SMTP_USERNAME = "your-email@gmail.com";
private static final String SMTP_PASSWORD = "your-app-password";
private static final String SMTP_HOST = "smtp.gmail.com";
private static final int SMTP_PORT = 587;
private static final boolean SMTP_TLS_ENABLED = true;
private static final boolean SMTP_SSL_ENABLED = false;
```

### Gmail App Password Setup

1. Enable 2-factor authentication on your Gmail account
2. Go to Google Account settings > Security > App passwords
3. Generate a new app password for this application
4. Use the generated password in the `SMTP_PASSWORD` field

## Installation & Building

1. **Clone the repository**
   ```bash
   git clone https://github.com/RayenMalouche/MCP-Server-For-Mailing-sdk-0.11.0.git
   cd MCP-Server-For-Mailing-Sdk-0.11.0
   ```

2. **Configure your email settings**
   ```bash
   # Edit Application.java with your credentials
   ```

3. **Build the project**
   ```bash
   mvn clean package -DskipTests
   ```

## Finding Required Paths

Before using the server, you'll need to identify several paths on your system:

### 1. Project Path (`<FULL_PATH_TO_PROJECT>`)
This is the absolute path to where you cloned/downloaded the project:

**Windows:**
- Right-click on the project folder → Properties → Location
- Example: `C:\Users\YourName\Documents\MCP-Server-For-Mailing-Sdk-0.11.0`

**macOS/Linux:**
- In terminal, navigate to project folder and run: `pwd`
- Example: `/home/username/MCP-Server-For-Mailing-Sdk-0.11.0`

### 2. Java Executable Path (`<JAVA_EXECUTABLE_PATH>`)
**Windows:**
```bash
where java
# Example output: C:\Program Files\Common Files\Oracle\Java\javapath\java.exe
```

**macOS/Linux:**
```bash
which java
# Example output: /usr/bin/java
# Or: /opt/homebrew/bin/java (Homebrew on macOS)
```

### 3. Java Home Path (`<JAVA_HOME_PATH>`)
**Windows:**
```bash
echo %JAVA_HOME%
# Or check: C:\Program Files\Java\jdk-21
# Or: C:\Program Files\Common Files\Oracle\Java\javapath
```

**macOS/Linux:**
```bash
echo $JAVA_HOME
# Or run: java -XshowSettings:properties 2>&1 | grep "java.home"
# Example: /usr/lib/jvm/java-21-openjdk
# macOS Homebrew: /opt/homebrew/opt/openjdk@21
```

### 4. Claude Desktop Config Location
**Windows:**
```
%APPDATA%\Claude\config.json
# Usually: C:\Users\YourName\AppData\Roaming\Claude\config.json
```

**macOS:**
```
~/Library/Application Support/Claude/config.json
```

**Linux:**
```
~/.config/Claude/config.json
```

## Usage

The server supports three different transport protocols. Choose the one that best fits your use case:

### 1. STDIO Transport (Recommended for Claude Desktop)

#### Testing with MCP Inspector
```bash
# 1. Start the server
java -jar target/RayenMalouche-0.0.1-SNAPSHOT.jar --stdio

# 2. In another terminal, run the inspector
npx @modelcontextprotocol/inspector java -jar "<FULL_PATH_TO_PROJECT>/target/RayenMalouche-0.0.1-SNAPSHOT.jar" --stdio
```

#### Claude Desktop Integration
Add this configuration to your Claude Desktop config file:

**For Windows:**
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

**For macOS/Linux:**
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

### 2. Server-Sent Events (SSE) Transport

#### Testing with MCP Inspector
```bash
# 1. Start the server
java -jar target/-0.0.1-SNAPSHOT.jar --sse
RayenMalouche
# 2. In another terminal, run the inspector
npx @modelcontextprotocol/inspector java -jar "<FULL_PATH_TO_PROJECT>/target/RayenMalouche-0.0.1-SNAPSHOT.jar" --sse
```

#### Direct MCP Client Connection
Connect your MCP client to the SSE endpoint:
```
http://localhost:45450/sse
```

### 3. Streamable HTTP Transport

#### Testing with MCP Inspector
```bash
# 1. Start the server
java -jar target/RayenMalouche-0.0.1-SNAPSHOT.jar --streamable-http

# 2. In another terminal, run the inspector
npx @modelcontextprotocol/inspector java -jar "<FULL_PATH_TO_PROJECT>/target/RayenMalouche-0.0.1-SNAPSHOT.jar" --streamable-http
```

#### REST API Testing

**Send Email**
```bash
curl -X POST http://localhost:45450/api/test-email \
  -H "Content-Type: application/json" \
  -d '{
    "to": "test@example.com",
    "subject": "Test Email",
    "body": "This is a test email from MCP server"
  }'
```

**Health Check**
```bash
curl http://localhost:45450/api/health
```

**Using Postman**
- Import the endpoints into Postman for easy testing
- Base URL: `http://localhost:45450`
- Endpoints: `/api/test-email` (POST), `/api/health` (GET)

## MCP Tool Parameters

The `send-email` tool accepts the following parameters:

- `to` (required): Recipient email address
- `subject` (required): Email subject line
- `body` (required): Email body content
- `cc` (optional): CC email address
- `bcc` (optional): BCC email address

## API Response Format

### Success Response
```json
{
  "status": "success",
  "message": "Email sent successfully",
  "details": {
    "to": "recipient@example.com",
    "subject": "Test Email",
    "cc": "cc@example.com",
    "bcc": "bcc@example.com"
  }
}
```

### Error Response
```json
{
  "status": "error",
  "message": "Failed to send email: [error details]",
  "errorType": "AuthenticationFailedException"
}
```

## Transport Protocol Comparison

| Transport | Use Case | Connection | Pros | Cons |
|-----------|----------|------------|------|------|
| **STDIO** | Claude Desktop, Direct integration | Process pipes | Simple, Direct | Requires process management |
| **SSE** | Web applications, Real-time | HTTP/SSE | Real-time updates | More complex setup |
| **Streamable HTTP** | Testing, REST clients | HTTP/JSON | Easy testing, Standard HTTP | Less efficient for real-time |

## Server Endpoints (HTTP Transports)

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/message` | POST | MCP protocol endpoint (Streamable HTTP) |
| `/sse` | GET | Server-Sent Events endpoint |
| `/api/test-email` | POST | REST endpoint for sending emails |
| `/api/health` | GET/POST | Health check endpoint |

## Dependencies

- **Spring Boot 3.5.4**: Application framework
- **MCP SDK 0.11.0**: Model Context Protocol implementation
- **Eclipse Jetty 12.0.18**: Embedded server
- **Jakarta Mail 2.0.1**: Email sending functionality
- **Jackson 2.15.2**: JSON processing

## Architecture

The application consists of:

1. **MCP Server**: Handles MCP protocol communication via multiple transports
2. **Email Service**: Core SMTP email sending functionality
3. **REST Endpoints**: Additional HTTP endpoints for testing and health checks
4. **Transport Layer**: Supports STDIO, SSE, and Streamable HTTP protocols

## Troubleshooting

### Common Issues

1. **Authentication Failed**
   - Ensure you're using an app-specific password for Gmail
   - Verify 2FA is enabled on your Google account
   - Double-check the username and password in `Application.java`

2. **Connection Refused**
   - Check if the SMTP server settings are correct
   - Verify firewall settings allow outbound connections on port 587
   - Ensure TLS settings match your SMTP provider requirements

3. **Port Already in Use**
   - The server uses port 45450 by default
   - Stop any existing processes using the port
   - Or modify the port in `Application.java`

4. **MCP Inspector Connection Issues**
   - Ensure the server is running before starting the inspector
   - Check that the correct transport argument is used
   - Verify the JAR file path is correct

### Debug Mode

To enable debug logging for email sending, add the following to your JVM arguments:
```bash
-Dmail.debug=true
```

### Testing Recommendations

1. **Start with STDIO**: Easiest to test and debug
2. **Use REST endpoints**: Test email functionality independently
3. **Verify with Inspector**: Confirm MCP protocol integration
4. **Check logs**: Monitor console output for detailed error messages

## Security Notes

- Store sensitive credentials securely (consider using environment variables in production)
- The current configuration includes hardcoded credentials for development purposes
- In production, use proper secret management solutions
- Consider implementing rate limiting for the REST endpoints

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test with all transport protocols
5. Submit a pull request

## License

This project uses Spring Boot and other open-source libraries. Please refer to individual dependency licenses for more information.

## Support

For issues and questions:
1. Check the troubleshooting section above
2. Review the server logs for detailed error messages
3. Verify your SMTP configuration
4. Test with the REST endpoint first before using MCP integration
5. Try different transport protocols to isolate issues