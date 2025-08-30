package com.smtp.mcp.mailing.server.RayenMalouche;

import com.smtp.mcp.mailing.server.RayenMalouche.Config.ConfigLoader;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletSseServerTransportProvider;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class RayenMaloucheApplication {

	// SMTP Configuration - will be loaded from application.properties
	private static String SMTP_USERNAME;
	private static String SMTP_PASSWORD;
	private static String SMTP_HOST;
	private static int SMTP_PORT;
	private static boolean SMTP_TLS_ENABLED;
	private static boolean SMTP_SSL_ENABLED;

	public static void main(String[] args) throws Exception {
		// Load configuration from application.properties
		loadEmailConfig();
		// Check if STDIO transport is requested
		boolean useStdio = args.length > 0 && "--stdio".equals(args[0]);
		boolean useStreamableHttp = args.length > 0 && "--streamable-http".equals(args[0]);

		if (useStdio) {
			System.err.println("Starting MCP server with STDIO transport...");
			startStdioServer();
		} else {
			System.out.println("Starting MCP server with HTTP/SSE transport...");
			startHttpServer(useStreamableHttp);
		}
	}

	private static void loadEmailConfig() {
		SMTP_USERNAME = ConfigLoader.getProperty("smtp.username");
		SMTP_PASSWORD = ConfigLoader.getProperty("smtp.password");
		SMTP_HOST = ConfigLoader.getProperty("smtp.host", "smtp.gmail.com");
		SMTP_PORT = ConfigLoader.getIntProperty("smtp.port", 587);
		SMTP_SSL_ENABLED = ConfigLoader.getBooleanProperty("smtp.ssl.enabled", false);
		SMTP_TLS_ENABLED = ConfigLoader.getBooleanProperty("smtp.tls.enabled", true);

		System.err.println("Email configuration loaded from application.properties");
	}

	private static void startStdioServer() {
		try {
			System.err.println("Initializing STDIO MCP server...");

			// 1) Create transport provider (ObjectMapper required)
			StdioServerTransportProvider transportProvider = new StdioServerTransportProvider(new ObjectMapper());

			// 2) Build synchronous MCP server with proper capabilities
			McpSyncServer syncServer = McpServer.sync(transportProvider)
					.serverInfo("smtp-email-server", "1.0.0")
					.capabilities(McpSchema.ServerCapabilities.builder()
							.tools(true)  // simpler form
							.logging()
							.build())
					// Register tools directly
					.tools(createEmailTool())
					.build();

			System.err.println("STDIO MCP server started. Awaiting requests...");

			// Block the main thread to keep the JVM running
			Thread.currentThread().join();

		} catch (Exception e) {
			System.err.println("Fatal error in STDIO server: " + e.getMessage());
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}

	private static void startHttpServer(boolean streamableHttp) throws Exception {
		// Create ObjectMapper
		ObjectMapper objectMapper = new ObjectMapper();

		// Create SSE transport provider with proper configuration
		HttpServletSseServerTransportProvider transportProvider;

		if (streamableHttp) {
			// For streamable-http, use standard MCP endpoints
			transportProvider = new HttpServletSseServerTransportProvider(objectMapper, "/message", "/sse");
		} else {
			// For regular HTTP, use root endpoints
			transportProvider = new HttpServletSseServerTransportProvider(objectMapper, "/", "/sse");
		}

		// Build synchronous MCP server
		McpSyncServer syncServer = McpServer.sync(transportProvider)
				.serverInfo("smtp-email-server", "1.0.0")
				.capabilities(McpSchema.ServerCapabilities.builder()
						.tools(true)  // Simplified
						.logging()
						.build())
				.tools(createEmailTool())  // Register tools directly in builder
				.build();

		// Configure Jetty server
		QueuedThreadPool threadPool = new QueuedThreadPool();
		threadPool.setName("mcp-server");

		Server server = new Server(threadPool);
		ServerConnector connector = new ServerConnector(server);
		connector.setPort(45450);
		server.addConnector(connector);

		ServletContextHandler context = new ServletContextHandler();
		context.setContextPath("/");

		// Add MCP transport servlet - this handles all MCP protocol communication
		context.addServlet(new ServletHolder(transportProvider), "/*");

		// Add testing servlets on different paths to avoid conflicts
		context.addServlet(new ServletHolder(new EmailTestServlet()), "/api/test-email");
		context.addServlet(new ServletHolder(new HealthServlet()), "/api/health");

		server.setHandler(context);

		// Start server
		server.start();
		System.err.println("=================================");
		System.err.println("MCP SMTP Email Server started on port 45450");
		if (streamableHttp) {
			System.err.println("Mode: Streamable HTTP (for MCP Inspector)");
			System.err.println("MCP endpoint: http://localhost:45450/message");
		} else {
			System.err.println("Mode: Standard HTTP/SSE");
			System.err.println("MCP endpoint: http://localhost:45450/");
		}
		System.err.println("SSE endpoint: http://localhost:45450/sse");
		System.err.println("Test endpoint: http://localhost:45450/api/test-email");
		System.err.println("Health check: http://localhost:45450/api/health");
		System.err.println("=================================");
		server.join();
	}

	private static McpServerFeatures.SyncToolSpecification createEmailTool() {
		return new McpServerFeatures.SyncToolSpecification(
				new McpSchema.Tool(
						"send-email",
						"Sends an email using SMTP with Jakarta Mail",
						"""
                                {
                                  "type": "object",
                                  "properties": {
                                    "to": {
                                      "type": "string",
                                      "description": "Recipient email address"
                                    },
                                    "subject": {
                                      "type": "string",
                                      "description": "Email subject line"
                                    },
                                    "body": {
                                      "type": "string",
                                      "description": "Email body content"
                                    },
                                    "cc": {
                                      "type": "string",
                                      "description": "CC email address (optional)"
                                    },
                                    "bcc": {
                                      "type": "string",
                                      "description": "BCC email address (optional)"
                                    }
                                  },
                                  "required": ["to", "subject", "body"]
                                }
                                """
				),
				(exchange, params) -> {
					try {
						String to = (String) params.get("to");
						String subject = (String) params.get("subject");
						String body = (String) params.get("body");
						String cc = (String) params.get("cc");
						String bcc = (String) params.get("bcc");

						// Log tool execution to stderr (not stdout)
						System.err.printf("Executing send-email tool: to=%s, subject=%s%n", to, subject);

						String result = sendEmailInternal(to, subject, body, cc, bcc);

						System.err.println("Email sent successfully");
						return new McpSchema.CallToolResult(
								List.of(new McpSchema.TextContent(result)),
								false
						);
					} catch (Exception e) {
						System.err.println("ERROR in send-email tool: " + e.getMessage());
						e.printStackTrace(System.err);
						return new McpSchema.CallToolResult(
								List.of(new McpSchema.TextContent(
										String.format("""
                                                {
                                                				"status": "error",
                                                				"message": "Failed to send email: %s",
                                                				"errorType": "%s"
                                                }""", e.getMessage(), e.getClass().getSimpleName())
								)),
								true
						);
					}
				}
		);
	}

	// Simple servlet for testing emails via REST
	public static class EmailTestServlet extends HttpServlet {
		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
			resp.setContentType("application/json");
			resp.setCharacterEncoding("UTF-8");

			// Add CORS headers
			resp.setHeader("Access-Control-Allow-Origin", "*");
			resp.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
			resp.setHeader("Access-Control-Allow-Headers", "Content-Type");

			try {
				// Read JSON from request body
				StringBuilder sb = new StringBuilder();
				BufferedReader reader = req.getReader();
				String line;
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}

				// Parse JSON manually (simple approach)
				String jsonBody = sb.toString();
				ObjectMapper mapper = new ObjectMapper();
				Map<String, Object> emailData = mapper.readValue(jsonBody, Map.class);

				String to = (String) emailData.get("to");
				String subject = (String) emailData.get("subject");
				String body = (String) emailData.get("body");
				String cc = (String) emailData.get("cc");
				String bcc = (String) emailData.get("bcc");

				if (to == null || subject == null || body == null) {
					resp.setStatus(400);
					resp.getWriter().write("{\"status\": \"error\", \"message\": \"Missing required fields: to, subject, body\"}");
					return;
				}

				String result = sendEmailInternal(to, subject, body, cc, bcc);
				resp.getWriter().write(result);

			} catch (Exception e) {
				resp.setStatus(500);
				resp.getWriter().write(String.format("{\"status\": \"error\", \"message\": \"%s\"}",
						e.getMessage().replace("\"", "\\\"")));
			}
		}

		@Override
		protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
			// Handle CORS preflight
			resp.setHeader("Access-Control-Allow-Origin", "*");
			resp.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
			resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
			resp.setStatus(200);
		}
	}

	// Health check servlet - now supports both GET and POST
	public static class HealthServlet extends HttpServlet {
		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
			sendHealthResponse(resp);
		}

		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
			sendHealthResponse(resp);
		}

		private void sendHealthResponse(HttpServletResponse resp) throws IOException {
			resp.setContentType("application/json");
			resp.setHeader("Access-Control-Allow-Origin", "*");
			resp.getWriter().write("{\"status\": \"healthy\", \"server\": \"SMTP MCP Server\", \"version\": \"1.0.0\"}");
		}
	}

	private static String sendEmailInternal(String to, String subject, String body, String cc, String bcc) throws Exception {
		// Configure SMTP properties
		Properties properties = new Properties();
		properties.put("mail.smtp.host", SMTP_HOST);
		properties.put("mail.smtp.port", String.valueOf(SMTP_PORT));
		properties.put("mail.smtp.auth", "true");

		if (SMTP_TLS_ENABLED) {
			properties.put("mail.smtp.starttls.enable", "true");
		}

		if (SMTP_SSL_ENABLED) {
			properties.put("mail.smtp.ssl.enable", "true");
		}

		// Create authenticator
		Authenticator authenticator = new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
			}
		};

		// Create session
		Session session = Session.getInstance(properties, authenticator);

		try {
			// Create message
			MimeMessage message = new MimeMessage(session);

			// Set sender
			message.setFrom(new InternetAddress(SMTP_USERNAME));

			// Set recipient
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

			// Set CC if provided
			if (cc != null && !cc.trim().isEmpty()) {
				message.addRecipient(Message.RecipientType.CC, new InternetAddress(cc));
			}

			// Set BCC if provided
			if (bcc != null && !bcc.trim().isEmpty()) {
				message.addRecipient(Message.RecipientType.BCC, new InternetAddress(bcc));
			}

			// Set subject and body
			message.setSubject(subject);
			message.setText(body);

			// Send email
			Transport.send(message);

			return String.format("""
                            {
                                "status": "success",
                                "message": "Email sent successfully",
                                "details": {
                                    "to": "%s",
                                    "subject": "%s",
                                    "cc": "%s",
                                    "bcc": "%s"
                                }
                            }""",
					escapeJsonString(to),
					escapeJsonString(subject),
					escapeJsonString(cc != null ? cc : ""),
					escapeJsonString(bcc != null ? bcc : ""));

		} catch (Exception e) {
			return String.format("""
                            {
                                "status": "error",
                                "message": "Failed to send email: %s",
                                "errorType": "%s"
                            }""",
					escapeJsonString(e.getMessage()),
					escapeJsonString(e.getClass().getSimpleName()));
		}
	}

	// Add this helper method to escape JSON strings
	private static String escapeJsonString(String input) {
		if (input == null) return "";
		return input.replace("\\", "\\\\")
				.replace("\"", "\\\"")
				.replace("\n", "\\n")
				.replace("\r", "\\r")
				.replace("\t", "\\t");
	}
}
