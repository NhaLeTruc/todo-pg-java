#!/bin/bash

# Performance Testing Script
# Tests application with 500 concurrent users using Apache Bench and custom load testing

set -e

BOLD='\033[1m'
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
API_BASE_URL="${API_BASE_URL:-http://localhost:8080}"
CONCURRENT_USERS="${CONCURRENT_USERS:-500}"
TEST_DURATION="${TEST_DURATION:-60}"  # seconds
RAMP_UP_TIME="${RAMP_UP_TIME:-10}"   # seconds

echo -e "${BOLD}================================${NC}"
echo -e "${BOLD}Performance Testing Script${NC}"
echo -e "${BOLD}================================${NC}"
echo ""
echo "Configuration:"
echo "  API Base URL: $API_BASE_URL"
echo "  Concurrent Users: $CONCURRENT_USERS"
echo "  Test Duration: ${TEST_DURATION}s"
echo "  Ramp-up Time: ${RAMP_UP_TIME}s"
echo ""

# Function to print success message
success() {
    echo -e "${GREEN}✓ $1${NC}"
}

# Function to print error message
error() {
    echo -e "${RED}✗ $1${NC}"
}

# Function to print warning message
warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

# Function to print info message
info() {
    echo -e "${BLUE}ℹ $1${NC}"
}

# Function to print section header
section() {
    echo -e "\n${BOLD}$1${NC}"
    echo "----------------------------------------"
}

# Check prerequisites
section "1. Checking Prerequisites"

# Check if API is reachable
if curl -sf "${API_BASE_URL}/actuator/health" > /dev/null 2>&1; then
    success "API is reachable at ${API_BASE_URL}"
else
    error "API is not reachable at ${API_BASE_URL}"
    echo "  Make sure the application is running"
    echo "  Run: docker compose up -d"
    exit 1
fi

# Check for Apache Bench (ab)
if command -v ab &> /dev/null; then
    success "Apache Bench (ab) found"
    AB_AVAILABLE=true
else
    warning "Apache Bench (ab) not found"
    echo "  Install: apt-get install apache2-utils (Ubuntu/Debian)"
    echo "  Install: yum install httpd-tools (CentOS/RHEL)"
    AB_AVAILABLE=false
fi

# Check for curl
if command -v curl &> /dev/null; then
    success "curl found"
else
    error "curl not found - required for testing"
    exit 1
fi

# Create results directory
RESULTS_DIR="performance-test-results-$(date +%Y%m%d-%H%M%S)"
mkdir -p "$RESULTS_DIR"
success "Results directory created: $RESULTS_DIR"

# Warmup
section "2. Warming up the Application"

echo "  Sending warmup requests..."
for i in {1..10}; do
    curl -sf "${API_BASE_URL}/actuator/health" > /dev/null 2>&1 || true
    sleep 0.1
done
success "Warmup complete"

# Test 1: Health Endpoint Performance
section "3. Test 1: Health Endpoint Performance"

if [ "$AB_AVAILABLE" = true ]; then
    echo "  Testing with 1000 requests, 100 concurrent..."
    ab -n 1000 -c 100 -g "$RESULTS_DIR/health-gnuplot.tsv" \
       "${API_BASE_URL}/actuator/health" > "$RESULTS_DIR/health-test.txt" 2>&1

    # Parse results
    if [ -f "$RESULTS_DIR/health-test.txt" ]; then
        RPS=$(grep "Requests per second" "$RESULTS_DIR/health-test.txt" | awk '{print $4}')
        MEAN_TIME=$(grep "Time per request.*mean" "$RESULTS_DIR/health-test.txt" | head -1 | awk '{print $4}')
        P50=$(grep "50%" "$RESULTS_DIR/health-test.txt" | awk '{print $2}')
        P95=$(grep "95%" "$RESULTS_DIR/health-test.txt" | awk '{print $2}')
        P99=$(grep "99%" "$RESULTS_DIR/health-test.txt" | awk '{print $2}')

        echo ""
        success "Health endpoint test complete"
        echo "  Requests/sec: $RPS"
        echo "  Mean time: ${MEAN_TIME}ms"
        echo "  50th percentile: ${P50}ms"
        echo "  95th percentile: ${P95}ms"
        echo "  99th percentile: ${P99}ms"
    fi
fi

# Test 2: API Endpoint Performance (without auth for simplicity)
section "4. Test 2: API Documentation Endpoint"

if [ "$AB_AVAILABLE" = true ]; then
    echo "  Testing Swagger UI endpoint with 500 requests, 50 concurrent..."
    ab -n 500 -c 50 -g "$RESULTS_DIR/swagger-gnuplot.tsv" \
       "${API_BASE_URL}/swagger-ui.html" > "$RESULTS_DIR/swagger-test.txt" 2>&1

    if [ -f "$RESULTS_DIR/swagger-test.txt" ]; then
        RPS=$(grep "Requests per second" "$RESULTS_DIR/swagger-test.txt" | awk '{print $4}')
        FAILED=$(grep "Failed requests" "$RESULTS_DIR/swagger-test.txt" | awk '{print $3}')

        echo ""
        if [ "$FAILED" = "0" ]; then
            success "Swagger UI test complete - 0 failures"
        else
            warning "Swagger UI test complete - $FAILED failures"
        fi
        echo "  Requests/sec: $RPS"
    fi
fi

# Test 3: Concurrent User Simulation
section "5. Test 3: Concurrent User Load Test (${CONCURRENT_USERS} users)"

info "Starting concurrent user simulation..."
echo "  This test simulates ${CONCURRENT_USERS} concurrent users"
echo "  accessing the health endpoint for ${TEST_DURATION} seconds"
echo ""

# Create a simple load test script
cat > "$RESULTS_DIR/load-test.sh" << 'LOADTEST_SCRIPT'
#!/bin/bash
URL="$1"
DURATION="$2"
USER_ID="$3"

END_TIME=$(($(date +%s) + DURATION))
REQUEST_COUNT=0
ERROR_COUNT=0

while [ $(date +%s) -lt $END_TIME ]; do
    if curl -sf "$URL" > /dev/null 2>&1; then
        ((REQUEST_COUNT++))
    else
        ((ERROR_COUNT++))
    fi
    sleep 0.1  # Small delay between requests
done

echo "$USER_ID,$REQUEST_COUNT,$ERROR_COUNT"
LOADTEST_SCRIPT

chmod +x "$RESULTS_DIR/load-test.sh"

# Run concurrent users
START_TIME=$(date +%s)
PIDS=()

for i in $(seq 1 $CONCURRENT_USERS); do
    "$RESULTS_DIR/load-test.sh" "${API_BASE_URL}/actuator/health" \
        "$TEST_DURATION" "$i" > "$RESULTS_DIR/user-$i.txt" 2>&1 &
    PIDS+=($!)

    # Ramp-up delay
    if [ $((i % 50)) -eq 0 ]; then
        echo "  Started $i users..."
        sleep $(echo "scale=2; $RAMP_UP_TIME / ($CONCURRENT_USERS / 50)" | bc)
    fi
done

success "All $CONCURRENT_USERS users started"
echo "  Waiting for test to complete ($TEST_DURATION seconds)..."

# Wait for all background processes
for pid in "${PIDS[@]}"; do
    wait "$pid" 2>/dev/null || true
done

END_TIME=$(date +%s)
TOTAL_TIME=$((END_TIME - START_TIME))

success "Load test complete"

# Analyze results
section "6. Analyzing Results"

TOTAL_REQUESTS=0
TOTAL_ERRORS=0
SUCCESSFUL_USERS=0

for i in $(seq 1 $CONCURRENT_USERS); do
    if [ -f "$RESULTS_DIR/user-$i.txt" ]; then
        USER_DATA=$(cat "$RESULTS_DIR/user-$i.txt")
        REQUESTS=$(echo "$USER_DATA" | cut -d',' -f2)
        ERRORS=$(echo "$USER_DATA" | cut -d',' -f3)

        TOTAL_REQUESTS=$((TOTAL_REQUESTS + REQUESTS))
        TOTAL_ERRORS=$((TOTAL_ERRORS + ERRORS))

        if [ "$ERRORS" -eq 0 ]; then
            ((SUCCESSFUL_USERS++))
        fi
    fi
done

# Calculate metrics
if [ $TOTAL_TIME -gt 0 ]; then
    RPS=$((TOTAL_REQUESTS / TOTAL_TIME))
else
    RPS=0
fi

ERROR_RATE=0
if [ $TOTAL_REQUESTS -gt 0 ]; then
    ERROR_RATE=$(echo "scale=2; ($TOTAL_ERRORS * 100) / $TOTAL_REQUESTS" | bc)
fi

# Print results
echo ""
echo -e "${BOLD}Performance Test Results:${NC}"
echo "  Total Test Duration: ${TOTAL_TIME}s"
echo "  Concurrent Users: $CONCURRENT_USERS"
echo "  Successful Users (0 errors): $SUCCESSFUL_USERS"
echo "  Total Requests: $TOTAL_REQUESTS"
echo "  Total Errors: $TOTAL_ERRORS"
echo "  Requests/Second: $RPS"
echo "  Error Rate: ${ERROR_RATE}%"
echo ""

# Performance benchmarks
if [ $RPS -gt 1000 ]; then
    success "Excellent performance: >1000 requests/sec"
elif [ $RPS -gt 500 ]; then
    success "Good performance: >500 requests/sec"
elif [ $RPS -gt 100 ]; then
    warning "Moderate performance: >100 requests/sec"
else
    warning "Low performance: <100 requests/sec"
fi

if [ "$ERROR_RATE" = "0.00" ] || [ "$TOTAL_ERRORS" -eq 0 ]; then
    success "Perfect reliability: 0% error rate"
elif (( $(echo "$ERROR_RATE < 1.0" | bc -l) )); then
    success "Good reliability: <1% error rate"
elif (( $(echo "$ERROR_RATE < 5.0" | bc -l) )); then
    warning "Moderate reliability: <5% error rate"
else
    error "Poor reliability: >5% error rate"
fi

# Check system metrics if available
section "7. System Metrics (if available)"

if command -v docker &> /dev/null; then
    info "Docker container stats:"
    echo ""
    docker stats --no-stream todoapp-backend 2>/dev/null || echo "  Backend container not running"
    echo ""
    docker stats --no-stream todoapp-postgres 2>/dev/null || echo "  Database container not running"
    echo ""
    docker stats --no-stream todoapp-redis 2>/dev/null || echo "  Redis container not running"
fi

# Generate summary report
section "8. Generating Summary Report"

cat > "$RESULTS_DIR/summary.md" << SUMMARY
# Performance Test Summary

**Date**: $(date)
**API Base URL**: $API_BASE_URL

## Configuration
- Concurrent Users: $CONCURRENT_USERS
- Test Duration: ${TEST_DURATION}s
- Ramp-up Time: ${RAMP_UP_TIME}s

## Results
- Total Test Duration: ${TOTAL_TIME}s
- Total Requests: $TOTAL_REQUESTS
- Total Errors: $TOTAL_ERRORS
- Requests/Second: $RPS
- Error Rate: ${ERROR_RATE}%
- Successful Users (0 errors): $SUCCESSFUL_USERS/$CONCURRENT_USERS

## Performance Rating
$(if [ $RPS -gt 1000 ]; then echo "✓ Excellent"; elif [ $RPS -gt 500 ]; then echo "✓ Good"; elif [ $RPS -gt 100 ]; then echo "⚠ Moderate"; else echo "✗ Needs Improvement"; fi)

## Reliability Rating
$(if [ "$TOTAL_ERRORS" -eq 0 ]; then echo "✓ Perfect (0% errors)"; elif (( $(echo "$ERROR_RATE < 1.0" | bc -l) )); then echo "✓ Good (<1% errors)"; elif (( $(echo "$ERROR_RATE < 5.0" | bc -l) )); then echo "⚠ Moderate (<5% errors)"; else echo "✗ Poor (>5% errors)"; fi)

## Recommendations
1. Monitor application metrics at ${API_BASE_URL}/actuator/metrics
2. Check database connection pool size and adjust if needed
3. Review cache hit rates in Redis
4. Consider horizontal scaling if RPS < 500
5. Investigate error logs if error rate > 1%

## Files Generated
- health-test.txt: Apache Bench results for health endpoint
- swagger-test.txt: Apache Bench results for Swagger UI
- load-test.sh: Concurrent user simulation script
- user-*.txt: Individual user results
- summary.md: This file

SUMMARY

success "Summary report generated: $RESULTS_DIR/summary.md"

# Final summary
section "Summary"

echo ""
echo -e "${GREEN}${BOLD}✓ Performance testing completed${NC}\n"
echo "Results saved to: $RESULTS_DIR/"
echo ""
echo "Next steps:"
echo "  1. Review detailed results in $RESULTS_DIR/summary.md"
echo "  2. Check application logs for any errors"
echo "  3. Monitor metrics at ${API_BASE_URL}/actuator/metrics"
echo "  4. Review database and cache performance"
echo ""

if [ "$ERROR_RATE" != "0.00" ] && [ "$TOTAL_ERRORS" -gt 0 ]; then
    echo -e "${YELLOW}Note: Errors were detected. Check application logs for details${NC}"
    echo ""
fi
