#!/bin/bash
export DATABASE_URL="postgresql://username:password@ep-xxxx.aws.neon.tech/neondb?sslmode=require"
export DB_USERNAME="your_username"
export DB_PASSWORD="your_password"
export PORT=8085
export ADMIN_USERNAME=admin
export ADMIN_PASSWORD=admin123
export JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
export CORS_ALLOWED_ORIGINS="http://localhost:5173,https://your-frontend-domain.vercel.app"

cd "$(dirname "$0")"
./mvnw spring-boot:run
