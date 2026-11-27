#!/usr/bin/env bash
# Test script for `git bisect run`.
# Exit 0 = good, Exit 1 = bad, Exit 125 = untestable (skip)
FILE="pricing.properties"
if [ ! -f "$FILE" ]; then
  exit 125
fi
if grep -q '^vip_discount_percent=10$' "$FILE"; then
  exit 0
fi
if grep -q '^vip_discount_percent=' "$FILE"; then
  exit 1
fi
exit 125
