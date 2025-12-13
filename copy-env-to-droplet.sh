#!/bin/bash
# ==============================================================================
# SCRIPT: Copy .env file to DigitalOcean Droplet
# ==============================================================================
# Usage: ./copy-env-to-droplet.sh YOUR_DROPLET_IP
# ==============================================================================

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}===================================================================${NC}"
echo -e "${BLUE}   PDCMS Backend - Copy .env to Droplet${NC}"
echo -e "${BLUE}===================================================================${NC}"
echo ""

# Check if IP address is provided
if [ -z "$1" ]; then
    echo -e "${RED}❌ Error: Droplet IP address not provided${NC}"
    echo -e "${YELLOW}Usage: ./copy-env-to-droplet.sh YOUR_DROPLET_IP${NC}"
    echo -e "${YELLOW}Example: ./copy-env-to-droplet.sh 134.209.100.50${NC}"
    exit 1
fi

DROPLET_IP=$1
USERNAME=${2:-root}
DEPLOY_PATH=${3:-/root/pdcms-be}

echo -e "${YELLOW}📋 Configuration:${NC}"
echo -e "   Droplet IP: ${GREEN}${DROPLET_IP}${NC}"
echo -e "   Username: ${GREEN}${USERNAME}${NC}"
echo -e "   Deploy Path: ${GREEN}${DEPLOY_PATH}${NC}"
echo ""

# Check if .env.production exists
if [ ! -f ".env.production" ]; then
    echo -e "${RED}❌ Error: .env.production file not found${NC}"
    echo -e "${YELLOW}Please make sure .env.production exists in the current directory${NC}"
    exit 1
fi

echo -e "${BLUE}🔍 Checking SSH connection...${NC}"
if ! ssh -o ConnectTimeout=5 ${USERNAME}@${DROPLET_IP} "echo '✅ SSH connection successful'"; then
    echo -e "${RED}❌ Error: Cannot connect to Droplet${NC}"
    echo -e "${YELLOW}Please check:${NC}"
    echo -e "   1. Droplet IP address is correct"
    echo -e "   2. SSH key is configured"
    echo -e "   3. Firewall allows SSH (port 22)"
    exit 1
fi

echo ""
echo -e "${BLUE}📁 Creating backup of existing .env...${NC}"
ssh ${USERNAME}@${DROPLET_IP} "cd ${DEPLOY_PATH} && [ -f .env ] && cp .env .env.backup.\$(date +%Y%m%d_%H%M%S) || echo 'No existing .env file'"

echo ""
echo -e "${BLUE}📤 Copying .env.production to Droplet...${NC}"
scp .env.production ${USERNAME}@${DROPLET_IP}:${DEPLOY_PATH}/.env

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ .env file copied successfully!${NC}"
else
    echo -e "${RED}❌ Error: Failed to copy .env file${NC}"
    exit 1
fi

echo ""
echo -e "${BLUE}🔒 Setting correct permissions...${NC}"
ssh ${USERNAME}@${DROPLET_IP} "cd ${DEPLOY_PATH} && chmod 600 .env && chown ${USERNAME}:${USERNAME} .env"

echo ""
echo -e "${BLUE}✅ Verifying .env file on Droplet...${NC}"
echo -e "${YELLOW}----------------------------------------${NC}"
ssh ${USERNAME}@${DROPLET_IP} "cd ${DEPLOY_PATH} && head -n 15 .env"
echo -e "${YELLOW}----------------------------------------${NC}"

echo ""
echo -e "${GREEN}🎉 Done! .env file is ready on Droplet${NC}"
echo ""
echo -e "${YELLOW}⚠️  IMPORTANT NEXT STEPS:${NC}"
echo -e "   1. ${BLUE}SSH into Droplet:${NC} ssh ${USERNAME}@${DROPLET_IP}"
echo -e "   2. ${BLUE}Edit .env:${NC} cd ${DEPLOY_PATH} && nano .env"
echo -e "   3. ${BLUE}Update these values:${NC}"
echo -e "      - DB_PASSWORD (generate: openssl rand -base64 32)"
echo -e "      - REDIS_PASSWORD (generate: openssl rand -base64 32)"
echo -e "      - JWT_SECRET (generate: openssl rand -base64 64)"
echo -e "      - FRONTEND_URL (your actual domain)"
echo -e "   4. ${BLUE}Save and test:${NC} docker-compose up -d"
echo ""
echo -e "${BLUE}===================================================================${NC}"
