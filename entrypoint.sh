#!/bin/sh

# Convert system environment variables to a .env file for the application
# This ensures that variables set in Dokploy UI are available via .env if needed
printenv | grep -E '^(DB_|ADMIN_|PORT|WEB_PORT|KMS_)' > .env

echo "Generated .env file from environment variables."

# Execute the application
exec java -jar app.jar
