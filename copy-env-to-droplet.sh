#!/bin/bash
# ==============================================================================
# SCRIPT: Copy .env to DigitalOcean Droplet (AUTO-CONFIGURED)
# ==============================================================================
# Usage: ./copy-env-to-droplet.sh YOUR_DROPLET_IP
#
# This script copies the AUTO-CONFIGURED .env.production file to your Droplet.
# NO MANUAL EDITING NEEDED - all values are from your project!
# ==============================================================================

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}===================================================================${NC}"
echo -e "${BLUE}   PDCMS Backend - Auto-Configured .env Deployment${NC}"
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
echo -e "${BLUE}📤 Copying auto-configured .env.production to Droplet...${NC}"
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
ssh ${USERNAME}@${DROPLET_IP} "cd ${DEPLOY_PATH} && head -n 20 .env"
echo -e "${YELLOW}----------------------------------------${NC}"

echo ""
echo -e "${BLUE}===================================================================${NC}"
echo -e "${GREEN}✅ HOÀN THÀNH! .env đã sẵn sàng trên Droplet!${NC}"
echo -e "${BLUE}===================================================================${NC}"
echo ""
echo -e "${YELLOW}📋 FILE .ENV ĐÃ ĐƯỢC AUTO-CONFIGURED:${NC}"
echo -e "   ✅ Database: root / 123456 / dental_clinic_db"
echo -e "   ✅ Redis: redis123"
echo -e "   ✅ JWT Secret: (từ SecurityConfig)"
echo -e "   ✅ Email: hellodenteeth@gmail.com"
echo -e "   ✅ Frontend: http://localhost:3000"
echo ""
echo -e "${GREEN}🚀 KHÔNG CẦN EDIT GÌ CẢ! Chỉ cần start Docker:${NC}"
echo ""
echo -e "   ${BLUE}ssh ${USERNAME}@${DROPLET_IP}${NC}"
echo -e "   ${BLUE}cd ${DEPLOY_PATH}${NC}"
echo -e "   ${BLUE}docker-compose up -d${NC}"
echo ""
echo -e "${YELLOW}⏳ Đợi 30-60 giây, sau đó check:${NC}"
echo -e "   ${BLUE}docker-compose ps${NC}"
echo -e "   ${BLUE}docker-compose logs -f app${NC}"
echo -e "   ${BLUE}curl http://localhost:8080/actuator/health${NC}"
echo ""
echo -e "${YELLOW}🎯 Sau đó push code để trigger GitHub Actions deployment!${NC}"
echo -e "   ${BLUE}git add .${NC}"
echo -e "   ${BLUE}git commit -m \"feat: production ready\"${NC}"
echo -e "   ${BLUE}git push origin main${NC}"
echo ""
echo -e "${BLUE}===================================================================${NC}"
echo ""
